package com.chatBot.ChatbotAi.DTO.Response;

import com.chatBot.ChatbotAi.models.ApiKey;

public class ApiKeyList {
    public Long id;
    public String apiKey;
    public boolean status;

    public ApiKeyList(ApiKey apiKey) {
        this.id = apiKey.getId();
        this.apiKey = apiKey.getApiKey().substring(0, 11) + "*".repeat(apiKey.getApiKey().length() - 11);
        this.status = apiKey.isActive();
    }
}
