package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatDto {
    @NotBlank(message = "title is required")
    @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
    private String message;
}
