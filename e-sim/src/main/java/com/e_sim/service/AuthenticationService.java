package com.e_sim.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.e_sim.dao.entity.PhoneNumber;
import com.e_sim.dao.entity.SignupRequest;
import com.e_sim.dao.entity.User;
import com.e_sim.dao.repository.RoleRepository;
import com.e_sim.dao.repository.SignupRequestRepository;
import com.e_sim.dao.repository.UserRepository;
import com.e_sim.dto.request.AuthenticationReq;
import com.e_sim.dto.request.OtpReq;
import com.e_sim.dto.request.RegisterUserReq;
import com.e_sim.dto.response.ApiRes;
import com.e_sim.dto.response.AuthenticationRes;
import com.e_sim.dto.response.UserRes;
import com.e_sim.dto.response.ValidationRes;
import com.e_sim.exception.DuplicateResourceException;
import com.e_sim.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final SignupRequestRepository signupRequestRepository;
    private final OtpImpl otpService;

    
    public ApiRes<AuthenticationRes> signIn(AuthenticationReq authenticationReq, HttpServletResponse response) {
        log.info("Sign-in attempt for username/email: {}", authenticationReq.getUsername());

        User user = userRepository.findByEmailOrUsername(authenticationReq.getUsername())
                .filter(u -> passwordEncoder.matches(authenticationReq.getPassword(), u.getPassword()))
                .orElseThrow(() -> {
                    log.error("Authentication failed for: {}", authenticationReq.getUsername());
                    return new UsernameNotFoundException("Invalid credentials");
                });

        String accessToken = jwtUtil.createToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        response.setHeader("Set-Cookie",
            "refreshToken=" + refreshToken +
            "; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=" + (7 * 24 * 60 * 60)
        );

        user.setLastLoginTime(LocalDateTime.now(ZoneId.of("Africa/Lagos")));
        userRepository.save(user);

        return ApiRes.success(new AuthenticationRes(accessToken, new UserRes(user), null), HttpStatus.OK);
    }


    public ApiRes<AuthenticationRes> refreshToken(String refreshToken, HttpServletResponse response) {
        if (Strings.isBlank(refreshToken)) {
            return ApiRes.error(null, HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtUtil.createToken(user);
        String newRefreshToken = jwtUtil.createRefreshToken(user);

        response.setHeader("Set-Cookie",
            "refreshToken=" + newRefreshToken +
            "; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=" + (7 * 24 * 60 * 60)
        );

        return ApiRes.success(new AuthenticationRes(newAccessToken, new UserRes(user), null), HttpStatus.OK);
    }


    @Transactional
    public ApiRes<String> signUp(RegisterUserReq req) {

        log.info("Signup initiated for email: {}", req.getEmail());

        // String normalizedEmail = req.getEmail().trim().toLowerCase(); 

         log.info("non-normaized email initiated for email: {}", req.getEmail());
        validateUserDoesNotExist(req.getUsername(), req.getEmail());
        Optional<SignupRequest> existingSignupOpt = signupRequestRepository.findByEmail(req.getEmail());
        SignupRequest signup;
        if (existingSignupOpt.isPresent()) {
            signup = existingSignupOpt.get();
            log.info("Existing signup request found for email: {}, updating record", req.getEmail());
            signup.setFirstname(req.getFirstname());
            signup.setLastname(req.getLastname());
            signup.setUsername(req.getUsername());
            signup.setPassword(passwordEncoder.encode(req.getPassword()));
            signup.setPhoneNumber(req.getPhoneNumber());
            signup.setCreatedAt(LocalDateTime.now());
        } else {
            signup = SignupRequest.builder()
                    .firstname(req.getFirstname())
                    .lastname(req.getLastname())
                    .email(req.getEmail())
                    .username(req.getUsername())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .phoneNumber(req.getPhoneNumber())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        signupRequestRepository.save(signup);
        OtpReq otpReq = OtpReq.builder()
                .email(req.getEmail())
                .build();
        otpService.sendOtp(otpReq);
        log.info("OTP sent successfully to {}", req.getEmail());
        return ApiRes.success(
                "OTP sent successfully. Please verify your email to complete registration.",
                HttpStatus.OK
        );
        //  signupRequestRepository.save(signup);

        // OtpReq otpReq = OtpReq.builder().email(req.getEmail()).build();
        // String otp = otpService.sendOtp(otpReq);  // returns the generated OTP

        // log.info("OTP generated successfully for {}", req.getEmail());

        // // Return OTP in the response temporarily
        // return ApiRes.success(
        //         "OTP generated successfully. Please verify your email to complete registration. OTP: " + otp,
        //         HttpStatus.OK
        // );
    }


    @Transactional
    public ApiRes<UserRes> verifyOtp(String otp) {
        log.info("Verifying OTP: {}", otp);

        String verifiedEmail = otpService.verifyOtp(OtpReq.builder().otp(otp).build());
        // String req.getEmail() = verifiedEmail.trim().toLowerCase(); 

        SignupRequest signup = signupRequestRepository.findByEmail(verifiedEmail)
            .orElseThrow(() -> new RuntimeException("No signup request found for email: " + verifiedEmail));

        User newUser = createUserFromSignup(signup);
            log.debug("New user created with ID: {}", newUser.getId());

        signupRequestRepository.delete(signup);
            log.info("User created successfully after OTP verification: {}", verifiedEmail);
            return ApiRes.success(new UserRes(newUser), HttpStatus.CREATED);
    }


    public ApiRes<ValidationRes> validateToken(String token) {
        if (Strings.isBlank(token)) {
            log.warn("Empty token provided");
            return ApiRes.error(new ValidationRes(null,"Token cannot be empty"),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            final String username = jwtUtil.getUsernameFromToken(token);
            if (Strings.isBlank(username)) {
                log.warn("No username found in token: {}", maskToken(token));
                return ApiRes.error(new ValidationRes(null,"Invalid token: No username found"),
                        HttpStatus.UNAUTHORIZED);
            }
            log.debug("Extracted username from token: {}", username);

            if (!jwtUtil.isNotTokenExpired(token)) {
                log.warn("Expired token for user: {}", username);
                return ApiRes.error(new ValidationRes(null,"Token has expired"),
                        HttpStatus.UNAUTHORIZED);
            }

            if (!userRepository.existsByUsernameOrEmail(username,username)) {
                log.warn("User not found in system: {}", username);
                return ApiRes.error(new ValidationRes(null,"User not found"),
                        HttpStatus.UNAUTHORIZED);
            }

            log.info("Token validation successful for user: {}", username);
            return ApiRes.success(new ValidationRes(username,"Token is valid"), HttpStatus.OK);
        } catch (JwtException e) {
            return ApiRes.error(new ValidationRes(null,"Invalid JWT token: " + e.getMessage()),
                    HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            return ApiRes.error(new ValidationRes(null,"Authentication failed: " + e.getMessage()),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ApiRes.error(new ValidationRes(null,"Token validation error: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsernameOrEmail(username, email)) {
            throw new DuplicateResourceException("Username or email already exists");
        }
    }

    private User createUserFromSignup(SignupRequest signup) {
        User newUser = User.builder()
                .firstname(signup.getFirstname())
                .lastname(signup.getLastname())
                .email(signup.getEmail())
                .username(signup.getUsername())
                .password(signup.getPassword()) 
                .build();

        newUser.addPhoneNumber(new PhoneNumber(signup.getPhoneNumber()));
        newUser.addRole(roleRepository.findDefaultRoles());

        return userRepository.save(newUser);
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "[TOKEN]";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
