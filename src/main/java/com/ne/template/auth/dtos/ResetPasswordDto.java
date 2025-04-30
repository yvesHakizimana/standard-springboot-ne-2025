package com.ne.template.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid.")
        String email,

        @Size(min = 6, max = 6, message = "OTP must be 6 digits long.")
        String otp,

        @Size(min = 8, max = 50, message = "Password must be at least 8 characters long")
        String newPassword
) {
}
