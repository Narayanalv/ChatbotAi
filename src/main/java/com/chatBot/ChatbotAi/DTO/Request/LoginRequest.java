package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    public String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    public String password;
}
