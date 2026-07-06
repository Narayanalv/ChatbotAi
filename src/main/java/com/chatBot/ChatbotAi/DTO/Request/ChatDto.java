package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatDto {
    @NotBlank(message = "Message is required")
    @Size(min = 5, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String message;
}
