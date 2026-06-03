package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.ChatLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findByChatBotIdOrderByCreatedAtDesc(Long chatBotId);

    Page<ChatLog> findByChatBotIdOrderByCreatedAtDesc(Long chatBotId, Pageable pageable);
}
