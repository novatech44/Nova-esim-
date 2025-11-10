package com.e_sim.controller;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.e_sim.dto.request.AuthenticationReq;
import com.e_sim.dto.request.OtpReq;
import com.e_sim.dto.request.RegisterUserReq;
import com.e_sim.dto.response.ApiRes;
import com.e_sim.dto.response.AuthenticationRes;
import com.e_sim.dto.response.ErrorResponse;
import com.e_sim.dto.response.UserRes;
import com.e_sim.dto.response.ValidationRes;
import com.e_sim.service.AuthenticationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

//     @Operation(
//             summary = "Authenticate user",
//             description = "Authenticates user credentials and returns JWT token",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200", description = "Authentication successful",
//                             content = @Content(schema = @Schema(implementation = ApiRes.class),
//                                     examples = @ExampleObject(value = "{\"responseCode\":\"200\",\"responseMessage\":\"success\",\"data\":{\"token\":\"eyJhbGciOi...\",\"user\":{\"username\":\"john_doe\"}}}"))
//                     ),
//                     @ApiResponse(responseCode = "401", description = "Invalid credentials",
//                             content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
//                     @ApiResponse(responseCode = "400", description = "Validation error",
//                             content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//             }
//     )
//     @PostMapping("/signin")
//     public ResponseEntity<ApiRes<AuthenticationRes>> signIn(
//             @Parameter(description = "authentication request payload", required = true,
//                     content = @Content(schema = @Schema(implementation = AuthenticationReq.class)))
//             @Valid @RequestBody AuthenticationReq authenticationReq) {

//         log.info("Sign-in request received for username: {}", authenticationReq.getUsername());
//         ApiRes<AuthenticationRes> response = authenticationService.signIn(authenticationReq);
//         return new ResponseEntity<>(response, response.getHttpStatus());
//     }
        @Operation(
                summary = "Authenticate user",
                description = "Authenticates user credentials and returns JWT token and sets refresh token in cookie",
                responses = {
                        @ApiResponse(
                                responseCode = "200", description = "Authentication successful",
                                content = @Content(schema = @Schema(implementation = ApiRes.class),
                                        examples = @ExampleObject(value = "{\"responseCode\":\"200\",\"responseMessage\":\"success\",\"data\":{\"accessToken\":\"eyJhbGciOi...\",\"user\":{\"username\":\"john_doe\"}}}"))
                        ),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
        )
        @PostMapping("/signin")
        public ResponseEntity<ApiRes<AuthenticationRes>> signIn(
                @Parameter(description = "authentication request payload", required = true,
                        content = @Content(schema = @Schema(implementation = AuthenticationReq.class)))
                @Valid @RequestBody AuthenticationReq authenticationReq,
                HttpServletResponse response) {

                log.info("Sign-in request received for username/email: {}", authenticationReq.getUsername());

                ApiRes<AuthenticationRes> apiRes = authenticationService.signIn(authenticationReq, response);

                return new ResponseEntity<>(apiRes, apiRes.getHttpStatus());
        }

        @Operation(
                summary = "Refresh access token using refresh token",
                description = "Generates a new access token and refresh token. Refresh token is sent via HttpOnly cookie.",
                responses = {
                        @ApiResponse(
                                responseCode = "200", description = "Token refreshed successfully",
                                content = @Content(schema = @Schema(implementation = ApiRes.class),
                                        examples = @ExampleObject(value = "{\"responseCode\":\"200\",\"responseMessage\":\"success\",\"data\":{\"accessToken\":\"eyJhbGciOi...\",\"user\":{\"username\":\"john_doe\"}}}"))
                        ),
                        @ApiResponse(responseCode = "401", description = "Refresh token missing or invalid",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
        )
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiRes<AuthenticationRes>> refreshToken(
                @CookieValue(value = "refreshToken", required = false) String refreshToken,
                HttpServletResponse response) {

        if (Strings.isBlank(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiRes.<AuthenticationRes>error(null, HttpStatus.UNAUTHORIZED));
        }

        ApiRes<AuthenticationRes> apiRes = authenticationService.refreshToken(refreshToken, response);

        return new ResponseEntity<>(apiRes, apiRes.getHttpStatus());
        }





    @Operation(
                summary = "Initiate user signup (OTP verification required)",
                description = "Stores signup request and sends OTP to the provided email for verification.",
                responses = {
                        @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                                content = @Content(schema = @Schema(implementation = ApiRes.class))),
                        @ApiResponse(responseCode = "400", description = "Validation error or user already exists",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
        )
        @PostMapping("/signup")
        public ResponseEntity<ApiRes<String>> signUp(
                        @Parameter(description = "User registration request payload", required = true,
                                content = @Content(schema = @Schema(implementation = RegisterUserReq.class)))
                        @Valid @RequestBody RegisterUserReq registerUserReq) {

                log.info("Signup request received for email: {}", registerUserReq.getEmail());
                ApiRes<String> response = authenticationService.signUp(registerUserReq);
                return new ResponseEntity<>(response, response.getHttpStatus());
                }


        @Operation(
                summary = "Verify OTP and complete user registration",
                description = "Verifies the OTP sent to the user's email. On success, finalizes registration and creates the actual user account.",
                responses = {
                        @ApiResponse(responseCode = "201", description = "User created successfully after OTP verification",
                                content = @Content(schema = @Schema(implementation = ApiRes.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired OTP",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
                }
        )
        @PostMapping("/verify-otp")
        public ResponseEntity<ApiRes<UserRes>> verifyOtp(
                @Parameter(description = "The OTP code received via email", required = true)
                @RequestBody OtpReq otpRequest) {

        log.info("Received OTP verification request for OTP: {}", otpRequest.getOtp());
        ApiRes<UserRes> response = authenticationService.verifyOtp(otpRequest.getOtp());
        return new ResponseEntity<>(response, response.getHttpStatus());
        }



        @Operation(
                summary = "Validate JWT token", description = "Validates the JWT token and checks user existence",
                security = @SecurityRequirement(name = "bearerAuth"),
                responses = {
                        @ApiResponse(responseCode = "200", description = "Token is valid",
                                content = @Content(schema = @Schema(implementation = ApiRes.class),
                                        examples = @ExampleObject(
                                                value = "{\"responseCode\":\"200\",\"responseMessage\":\"success\",\"data\":{\"username\":\"john_doe\",\"message\":\"Token is valid\"}}"))),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired token",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Missing token",
                                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                }
        )
        @GetMapping("/validate-token")
        public ResponseEntity<ApiRes<ValidationRes>> validateToken(
                @Parameter(description = "token", required = true)
                @RequestHeader(name = "Authorization") String authHeader) {

                log.info("Token validation request received");
                ApiRes<ValidationRes> response = authenticationService.validateToken(authHeader.substring(7));
                return new ResponseEntity<>(response, response.getHttpStatus());
        }


}
