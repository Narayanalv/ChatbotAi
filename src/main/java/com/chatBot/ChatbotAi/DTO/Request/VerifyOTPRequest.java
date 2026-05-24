package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyOTPRequest {
    @NotBlank(message = "OTP code is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be exactly 6 numeric digits"
    )
    private String otp;
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
}
