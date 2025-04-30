package com.ne.template.auth;

import com.ne.template.auth.dtos.*;
import com.ne.template.commons.exceptions.BadRequestException;
import com.ne.template.email.EmailService;
import com.ne.template.users.UserService;
import com.ne.template.users.dtos.UserResponseDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;


    @PostMapping("/register")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody
                                                            RegisterRequestDto user, UriComponentsBuilder uriBuilder){
        var userResponse = userService.createUser(user);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userResponse.id()).toUri();
        // Use otp service to send otp to a registered user
        var otpToSend = otpService.generateOtp(userResponse.email(), OtpType.VERIFY_ACCOUNT);

        // Send email to the user with the OTP
        emailService.sendAccountVerificationEmail(userResponse.email(), userResponse.firstName(), otpToSend);
        return ResponseEntity.created(uri).body(userResponse);
    }

    @PatchMapping("/verify-account")
    @RateLimiter(name = "otp-rate-limiter")
    ResponseEntity<?> verifyAccount(@Valid @RequestBody VerifyAccountDto verifyAccountRequest){
        if(!otpService.verifyOtp(verifyAccountRequest.email(), verifyAccountRequest.otp(), OtpType.VERIFY_ACCOUNT))
            throw new BadRequestException("Invalid email or OTP");
        userService.activateUserAccount(verifyAccountRequest.email());
        return ResponseEntity.ok("Account Activated successfully");
    }

    @PostMapping("/initiate-password-reset")
    ResponseEntity<?> initiatePasswordReset(@Valid @RequestBody InitiatePasswordResetDto initiateRequest){
        var otpToSend = otpService.generateOtp(initiateRequest.email(), OtpType.RESET_PASSWORD);
        var user = userService.findByEmail(initiateRequest.email());
        emailService.sendResetPasswordOtp(user.getEmail(), user.getFirstName(), otpToSend);
        return ResponseEntity.ok("If your email is registered, you will receive an email with instructions to reset your password.");
    }


    @PatchMapping("/reset-password")
    @RateLimiter(name = "auth-rate-limiter")
    ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordRequest){
        if(!otpService.verifyOtp(resetPasswordRequest.email(), resetPasswordRequest.otp(), OtpType.RESET_PASSWORD))
            throw new BadRequestException("Invalid email or OTP");
        userService.changeUserPassword(resetPasswordRequest.email(), resetPasswordRequest.newPassword());
        return ResponseEntity.ok("Password reset went successfully you can login with your new password.");
    }

    @PostMapping("/login")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        var loginResult = authService.login(loginRequestDto, response);
        return ResponseEntity.ok(new LoginResponse(loginResult.accessToken()));
    }

}
