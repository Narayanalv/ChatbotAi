package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetPassword {
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
}
