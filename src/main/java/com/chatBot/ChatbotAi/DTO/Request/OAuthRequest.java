package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Data
public class OAuthRequest {
    @NotBlank(message = "Authentication Failed 1")
    private String token;
}
