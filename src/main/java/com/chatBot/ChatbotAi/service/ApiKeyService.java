package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.ApiKey;
import com.chatBot.ChatbotAi.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public ApiKey save(ApiKey apiKey) {
        return apiKeyRepository.save(apiKey);
    }
    public Optional<ApiKey> getApiKeyById(Long id) {
        return apiKeyRepository.findById(id);
    }

    public Optional<ApiKey> getApiKeyByApiKey(String apiKey) {
        return apiKeyRepository.getApiKeyByApiKey(apiKey);
    }
}
