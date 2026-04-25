package com.chatBot.ChatbotAi.DTO.Response;

import lombok.Getter;

@Getter
public class UserDetailsResponse extends  Response {
    private final String name;
    private final String email;
    UserDetailsResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
