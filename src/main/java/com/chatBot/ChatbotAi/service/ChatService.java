package com.chatBot.ChatbotAi.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public ChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = chatModel;
    }

    public String chat(String userMessage) {
        try {
            return chatModel.call(userMessage);
        } catch (Exception e) {
            log.error("Gemini API chat call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Chat generation failed: " + getRootCauseMessage(e), e);
        }
    }

    public String chatWithContext(String topic, String context, String question) {
        String prompt = """
                You are a helpful, conversational AI assistant specializing in: %s.
                
                You have been provided with specific knowledge (Context) to answer the user's question.
                
                Rules:
                1. Answer directly and naturally, as if the Context is your own built-in knowledge.
                2. Do NOT mention "the provided text", "the document", "the PDF", or say things like "The text says...". Just state the facts.
                3. Keep your answers concise, friendly, and to the point. Do not write long essays.
                4. Base your answer strictly on the Context. Do not make up facts.
                5. If the Context does not contain the answer, politely say something like "I'm sorry, I don't have that information." Do not summarize what the context does contain instead.
                Context:
                %s
                Question:
                %s
                """.formatted(topic, context, question);
        log.debug("Sending prompt to Gemini (length={})", prompt.length());
        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            log.error("Gemini API chatWithContext failed: {}", e.getMessage(), e);
            throw new RuntimeException("Chat generation failed: " + getRootCauseMessage(e), e);
        }
    }

    public Flux<String> streamChat(String context, String question) {
        String prompt = """
                You are a helpful, conversational AI assistant.
                
                You have been provided with specific knowledge (Context) to answer the user's question.
                
                Rules:
                1. Answer directly and naturally, as if the Context is your own built-in knowledge.
                2. Do NOT mention "the provided text", "the document", "the PDF", or say things like "The text says...". Just state the facts.
                3. Keep your answers concise, friendly, and to the point. Do not write long essays.
                4. Base your answer strictly on the Context. Do not make up facts.
                5. If the Context does not contain the answer, politely say something like "I'm sorry, I don't have that information." Do not summarize what the context does contain instead.
                
                Context:
                %s
                
                Question:
                %s
                
                Answer:
                """.formatted(context, question);

        return streamingChatModel.stream(prompt);
    }

    private String getRootCauseMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}
