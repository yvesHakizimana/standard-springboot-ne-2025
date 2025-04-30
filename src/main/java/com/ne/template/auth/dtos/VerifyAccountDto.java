package com.ne.template.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyAccountDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid.")
        String email,

        @Size(min = 6, max = 6, message = "OTP must be 6 digits long.")
        String otp
) {
}
