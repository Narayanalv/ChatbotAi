package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetPassword {
    @Email
    @NotBlank
    private String email;
}
