package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.ChatLog;
import com.chatBot.ChatbotAi.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatLogService {
    private final ChatLogRepository chatLogRepository;

    public ChatLog save(ChatLog chatLog) {
        return chatLogRepository.save(chatLog);
    }

    public List<ChatLog> getHistoryByChatBotId(Long chatBotId) {
        return chatLogRepository.findByChatBotIdOrderByCreatedAtDesc(chatBotId);
    }

    public Page<ChatLog> getHistoryByChatBotId(Long chatBotId, int page, int size) {
        return chatLogRepository.findByChatBotIdOrderByCreatedAtDesc(chatBotId, PageRequest.of(page, size));
    }
}
