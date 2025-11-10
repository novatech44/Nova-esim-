package com.e_sim.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.io.Serializable;

@Data
public class RegisterUserReq implements Serializable {

    @Schema(example = "novastarD123")
    @Pattern(
            message = "username must be 5-20 characters and Requires at least 1 lowercase, 1 uppercase, and 1 number",
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{5,20}$")
    private String username;

    @Schema(example = "novastarD123@gmail.com")
    @Pattern(
            message = "email is in invalid format",
            regexp = "^[\\w.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$"
    )
    @NotBlank(message = "email is required")
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "password must be 8-20 chars with 1 uppercase, 1 lowercase, 1 number, and 1 special character (@$!%*?&)"
    )
    @NotBlank(message = "password is required")
    private String password;

    @Schema(example = "nova")
    @Pattern(
            message = "firstname must be 2-50 characters and can include letters, hyphens, or apostrophes",
            regexp = "^[A-Za-z](?:[A-Za-z'-]{0,48}[A-Za-z])?$"
    )
    @NotBlank(message = "firstname is required")
    private String firstname;

    @Schema(example = "tech")
    @Pattern(
            message = "lastname must be 2-50 characters and can include letters, hyphens, or apostrophes",
            regexp = "^[A-Za-z](?:[A-Za-z'-]{0,48}[A-Za-z])?$"
    )
    @NotBlank(message = "lastname is required")
    private String lastname;

    @Pattern(regexp = "^(0[7-9][0-1]\\d{8})$",
            message = "Invalid Nigerian phone number (e.g., 08123456789)")
    private String phoneNumber;
}
