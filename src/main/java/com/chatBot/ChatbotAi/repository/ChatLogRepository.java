package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.ChatLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLogRepository extends CrudRepository<ChatLog, String> {
}
