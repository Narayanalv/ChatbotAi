package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePassword {
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    private String password;
    @NotBlank(message = "Confirm Password is required")
    @Size(min = 8, max = 30, message = "Confirm Password must be between 8 and 30 characters")
    private String confirmPassword;
}
