package com.e_sim.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthenticationReq {

    @JsonAlias({"userName", "user_name", "username", "email", "user", "userEmail"})
    @NotBlank(message = "Username or email is required")
    @Size(min = 5, max = 100, message = "Username/email must be between 5-100 characters")
    private String username;

    @JsonAlias({"passWord", "pass_word", "password", "pwd", "pass"})
    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,20}$",
            message = "Password must be 8-20 characters with at least 1 uppercase, 1 lowercase, 1 number, and 1 special character (@$!%*?&#)"
    )
    private String password;
}