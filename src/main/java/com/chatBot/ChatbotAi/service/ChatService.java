package com.chatBot.ChatbotAi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = chatModel;
    }

    public String chat(String userMessage) {
        return chatModel.call(userMessage);
    }

    public String chatWithContext(String context, String question) {
        String prompt = """
                You are a helpful assistant. Answer using only the context below.
                
                Context:
                %s
                
                Question: %s
                """.formatted(context, question);

        return chatModel.call(prompt);
    }

    public Flux<String> streamChat(String context, String question) {
        String prompt = """
                You are a helpful assistant. Answer using only the context below.
                
                Context:
                %s
                
                Question: %s
                """.formatted(context, question);

        return streamingChatModel.stream(prompt);
    }
}
