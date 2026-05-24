package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.ApiKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    @Query("SELECT u FROM ApiKey u WHERE u.apiKey = :apiKey AND u.visible = true AND u.active = true")
    public Optional<ApiKey> getApiKeyByApiKey(String apiKey);

    public List<ApiKey> findAll();

    public ApiKey findByChatBotId(Long chatBotId);

    public Optional<ApiKey> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE ApiKey u SET u.active = :toStatus WHERE u.id = :id and u.active = :fromStatus")
    public int updateApiKeyStatus(Long id, boolean fromStatus, boolean toStatus);

    @Query("SELECT u FROM ApiKey u WHERE u.chatBotId = :chatBotId AND u.visible = :status")
    Optional<ApiKey> getVisibleApikey(Long chatBotId, boolean status);

    @Modifying
    @Transactional
    @Query("UPDATE ApiKey u SET u.visible = :status WHERE u.id = :id")
    public int updateVisible(Long id, boolean status);
}
