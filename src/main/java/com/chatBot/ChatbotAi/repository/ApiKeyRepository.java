package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.ApiKey;
import com.cloudinary.Api;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    public Optional<ApiKey> getApiKeyByApiKey(String apiKey);
}
