package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.ChatLog;
import com.chatBot.ChatbotAi.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatLogService {
    private final ChatLogRepository chatLogRepository;

    public ChatLog save(ChatLog chatLog) {
        return chatLogRepository.save(chatLog);
    }
}
