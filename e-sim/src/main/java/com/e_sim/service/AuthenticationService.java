package com.e_sim.service;

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
import com.e_sim.dao.entity.User;
import com.e_sim.dao.repository.RoleRepository;
import com.e_sim.dao.repository.UserRepository;
import com.e_sim.dto.request.AuthenticationReq;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public ApiRes<AuthenticationRes> signIn(AuthenticationReq authenticationReq) {
        log.info("Sign-in attempt for username/email: {}", authenticationReq.getUsername());

        User user = userRepository.findByEmailOrUsername(authenticationReq.getUsername())
                .filter(value -> {
                    boolean passwordMatches = passwordEncoder.matches(
                            authenticationReq.getPassword(),
                            value.getPassword()
                    );
                    if (!passwordMatches) {
                        log.warn("Password mismatch for user: {}", authenticationReq.getUsername());
                    }
                    return passwordMatches;
                })
                .orElseThrow(() -> {
                    log.error("Authentication failed for: {}", authenticationReq.getUsername());
                    return new UsernameNotFoundException("Invalid credentials");
                });

        String tokenValue = jwtUtil.createToken(user);
        log.debug("JWT token generated for user ID: {}", user.getId());

        user.setLastLoginTime(LocalDateTime.now(ZoneId.of("Africa/Lagos")));
        userRepository.save(user);

        final UserRes userRes = new UserRes(user);
        log.info("Successful authentication for user ID: {}", user.getId());

        return ApiRes.success(new AuthenticationRes(tokenValue, userRes), HttpStatus.OK);
    }

    @Transactional
    public ApiRes<UserRes> signUp(RegisterUserReq registerUserReq) {
        log.info("Registration attempt for username: {}", registerUserReq.getUsername());

        validateUserDoesNotExist(registerUserReq.getUsername(), registerUserReq.getEmail());

        final var newUser = createUserFromRequest(registerUserReq);
        log.debug("New user created with ID: {}", newUser.getId());

        final var userRes = new UserRes(newUser);
        log.info("Successful registration for user ID: {}", newUser.getId());

        return ApiRes.success(userRes, HttpStatus.CREATED);
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

    private User createUserFromRequest(RegisterUserReq request) {

        User newUser = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        newUser.addPhoneNumber(new PhoneNumber(request.getPhoneNumber()));
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
