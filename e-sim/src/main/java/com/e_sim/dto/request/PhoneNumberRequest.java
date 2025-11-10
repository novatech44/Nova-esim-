package com.e_sim.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneNumberRequest(@NotBlank(message = "number is required") @Pattern(regexp = "^(0[7-9][0-1]\\d{8})$",
                                         message = "Invalid Nigerian phone number (e.g., 08123456789)")
                                 String number) {}
