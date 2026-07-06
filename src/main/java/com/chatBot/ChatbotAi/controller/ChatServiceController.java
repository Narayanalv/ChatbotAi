package com.chatBot.ChatbotAi.controller;

import com.chatBot.ChatbotAi.DTO.Request.ChatDto;
import com.chatBot.ChatbotAi.DTO.Response.ChatResponse;
import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.models.*;
import com.chatBot.ChatbotAi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;

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
    private final ChatLogService chatLogService;

    @PostMapping("/")
    public ResponseEntity<ChatResponse> getChatResponse(@RequestBody @Valid ChatDto chatDto,
            @AuthenticationPrincipal ChatBot chatBot) {
        List<RagChunk> ragChunks;
        StringBuilder context;
        ChatResponse response = new ChatResponse();
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext().getAuthentication();
        try {
            assert auth != null;
            ApiKey apiKey = (ApiKey) auth.getDetails();
        } catch (Exception e) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        if (chatBot == null) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        StringBuilder message = new StringBuilder(chatDto.getMessage());
        if (chatBot.getChunkedData() != 2) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        ChatLog chatLog = new ChatLog();
        chatLog.setChatBotId(chatBot.getId());
        chatLog.setMessage(chatDto.getMessage());
        if (message.isEmpty()) {
            context = new StringBuilder("write a wellcome message for the topic chatbot " + chatBot.getTopic());
        } else {
            ragChunks = ragChunkService.searchWithThreshold(message.toString(), chatBot.getId());
            context = new StringBuilder();
            int topK = ragChunks.size();
            Long[] ids = new Long[topK];
            int i = 0;
            for (RagChunk rag : ragChunks) {
                ids[i] = rag.getId();
                i++;
                if ("IMAGE".equals(rag.getChunkType())) {
                    context.append("[Image: ").append(rag.getImageUrl()).append("] ")
                            .append(rag.getTextChunk()).append("\n\n---\n\n");
                } else {
                    context.append(rag.getTextChunk()).append("\n\n---\n\n");
                }
            }
            chatLog.setContextIds(Arrays.toString(ids));
            System.out.println(context.toString());
        }
        String chat = chatService.chatWithContext(chatBot.getTopic(), context.toString(), message.toString());
        chatLog.setResponseMessage(chat);
        chatLog.setApiKeyId(chatBot.getId());
        chatLogService.save(chatLog);
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
    public ResponseEntity<ChatResponse> testChatResponse(@RequestBody @Valid ChatDto chatDto) {
        String chat = chatService.chat(chatDto.getMessage());
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setMessage(chat);
        System.out.println(chat);
        System.out.println(chatService.chatWithContext("", "test", chat));
        return ResponseEntity.ok(chatResponse);
    }

    @PostMapping("/Chatbot/{chatBotId}")
    public ResponseEntity<Response> testChat(@AuthenticationPrincipal User user, @RequestBody @Valid ChatDto chatDto,
            @PathVariable("chatBotId") Long id) {
        ChatResponse chatResponse = new ChatResponse();
        List<RagChunk> ragChunks;
        StringBuilder context;
        boolean exist = chatBotService.existsChatBotToUserId(id, user.getId());
        ChatResponse response = new ChatResponse();
        StringBuilder message = new StringBuilder(chatDto.getMessage());
        if (!exist) {
            response.setStatus(401);
            response.setMessage("Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        Optional<ChatBot> chatBot = chatBotService.getChatBotById(id);
        if (chatBot.isEmpty() || chatBot.get().getChunkedData() != 2) {
            response.setStatus(400);
            if (chatBot.isEmpty()) {
                response.setMessage("Chatbot not found");
            } else {
                response.setMessage("ChatBot is ready");
            }
            return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
        }

        ChatLog chatLog = new ChatLog();
        chatLog.setChatBotId(id);
        chatLog.setMessage(chatDto.getMessage());

        if (message.isEmpty()) {
            context = new StringBuilder(chatBot.get().getTopic());
        } else {
            ragChunks = ragChunkService.searchWithThreshold(message.toString(), id);
            context = new StringBuilder();
            int topK = ragChunks.size();
            Long[] ids = new Long[topK];
            int i = 0;
            for (RagChunk rag : ragChunks) {
                ids[i] = rag.getId();
                i++;
                if ("IMAGE".equals(rag.getChunkType())) {
                    context.append("[Image: ").append(rag.getImageUrl()).append("] ")
                            .append(rag.getTextChunk()).append("\n\n---\n\n");
                } else {
                    context.append(rag.getTextChunk()).append("\n\n---\n\n");
                }
            }
            chatLog.setContextIds(Arrays.toString(ids));
            System.out.println(context.toString());
        }
        String chat = chatService.chatWithContext(chatBot.get().getTopic(), context.toString(), message.toString());
        chatLog.setResponseMessage(chat);
        chatLogService.save(chatLog);

        chatResponse.setMessageText(chat);
        System.out.println(chat);
        return ResponseEntity.ok(chatResponse);
    }
}
