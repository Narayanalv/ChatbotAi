package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.ChatBot;
import com.chatBot.ChatbotAi.repository.ChatBotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatBotService {
    private final ChatBotRepository chatBotRepository;

    public ChatBot createChat(Long id, String title, String topic, String image) {
        ChatBot chatBot = new ChatBot(id, title, topic, image);
        return chatBotRepository.save(chatBot);
    }

    public Optional<ChatBot> getPendingChunk(int chunked) {
        return chatBotRepository.findFirstByChunkedData(chunked);
    }

    public int updateChatBotChunkStart(Long id) {
        return chatBotRepository.updateChunkedData(1, 0, id);
    }

    public int updateChatBotChunkComplete(Long id) {
        return chatBotRepository.updateChunkedData(2, 1, id);
    }

    public Optional<ChatBot> getChatBotById(Long id) {
        return chatBotRepository.findById(id);
    }

    public Optional<List<ChatBot>> getAllChatBots(Long userId) {
        return chatBotRepository.getBotsByUserId(userId);
    }

    public boolean existsChatBotToUserId(Long chatBotId, Long userId) {
        return chatBotRepository.existsChatBotToUserId(chatBotId, true, userId);
    }
}
