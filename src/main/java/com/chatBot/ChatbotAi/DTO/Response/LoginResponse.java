package com.chatBot.ChatbotAi.DTO.Response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse extends Response {
    private String accessToken;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
