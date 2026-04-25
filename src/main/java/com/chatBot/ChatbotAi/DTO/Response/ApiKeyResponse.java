package com.chatBot.ChatbotAi.DTO.Response;

import lombok.Setter;

import java.util.List;

@Setter
public class ApiKeyResponse extends Response {
    public List<ApiKeyList> apiKeyList;
}
