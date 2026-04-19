package com.chatBot.ChatbotAi.controller;

import com.chatBot.ChatbotAi.DTO.Request.ChatDto;
import com.chatBot.ChatbotAi.DTO.Response.ChatResponse;
import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.models.ApiKey;
import com.chatBot.ChatbotAi.models.ChatBot;
import com.chatBot.ChatbotAi.models.RagChunk;
import com.chatBot.ChatbotAi.service.ApiKeyService;
import com.chatBot.ChatbotAi.service.ChatBotService;
import com.chatBot.ChatbotAi.service.ChatService;
import com.chatBot.ChatbotAi.service.RagChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatServiceController {
    private final ApiKeyService apiKeyService;
    private final ChatBotService chatBotService;
    private final RagChunkService ragChunkService;
    private final ChatService chatService;
    private final EmbeddingModel embeddingModel;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> getChatResponse(@RequestBody ChatDto chatDto,
                                                        @RequestHeader("Authorization") String apiKey) {
        List<RagChunk> ragChunks;
        StringBuilder context;
        Optional<ApiKey> apiKeyData = apiKeyService.getApiKeyByApiKey(apiKey);
        ChatResponse response = new ChatResponse();
        StringBuilder message = new StringBuilder(chatDto.getMessage());
        if (apiKeyData.isEmpty()) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        Optional<ChatBot> chatBot = chatBotService.getChatBotById(apiKeyData.get().getChatBotId());
        if (chatBot.isEmpty() || chatBot.get().getChunkedData() != 2) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        if (message.isEmpty()) {
            context = new StringBuilder("write a wellcome message for the topic chatbot " + chatBot.get().getTopic());
        } else {
            ragChunks = ragChunkService.search(message.toString(), apiKeyData.get().getChatBotId(), 5);
            context = new StringBuilder();
            for (RagChunk rag : ragChunks) {
                context.append(rag.getTextChunk());
            }
        }
        String chat = chatService.chatWithContext(context.toString(), message.toString());
        response.setStatus(200);
        response.setMessage("success");
        response.setMessageText(chat);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String context,
            @RequestParam String question) {
        return chatService.streamChat(context, question);
    }


    @PostMapping("/test")
    public ResponseEntity<ChatResponse> testChatResponse(@RequestBody ChatDto chatDto) {
        String chat = chatService.chat(chatDto.getMessage());
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setMessage(chat);
        System.out.println(chat);
        System.out.println(chatService.chatWithContext("test", chat));
        return ResponseEntity.ok(chatResponse);
    }
}
