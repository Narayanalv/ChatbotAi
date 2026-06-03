package com.chatBot.ChatbotAi.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

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
                You are a helpful, knowledgeable AI assistant specializing in: %s.
                
                You have been provided with specific knowledge (Context) to answer the user's question.
                
                Rules:
                1. Answer directly and naturally, as if the Context is your own built-in knowledge.
                2. Do NOT mention "the provided text", "the document", "the PDF", or say things like "The text says...". Just state the facts.
                3. Provide detailed, well-structured answers. Use proper formatting:
                   - Use headings (## or ###) to organize sections when appropriate.
                   - Use bullet points or numbered lists for steps or multiple items.
                   - Include code examples with proper syntax highlighting (```language) when the topic involves programming.
                   - Add brief explanations for each step or concept.
                4. Base your answer strictly on the Context. Do not make up facts.
                5. If the Context contains code or technical details, provide the COMPLETE code with proper formatting, not just snippets.
                6. If the Context does not contain the answer, politely say something like "I'm sorry, I don't have that information." Do not summarize what the context does contain instead.
                7. When relevant, suggest next steps or related topics the user might want to explore.
                
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

    public String describeImage(String imageUrl) {
        try {
            Media imageMedia = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl));
            UserMessage userMessage = UserMessage.builder()
                    .text("Describe this PDF page image in detail. Include all text content, diagrams, charts, tables, " +
                          "and visual elements you can see. This description will be used for search indexing.")
                    .media(imageMedia)
                    .build();
            String response = chatModel.call(new Prompt(List.of(userMessage)))
                    .getResult().getOutput().getText();
            log.info("Image described successfully (length={})", response.length());
            return response;
        } catch (Exception e) {
            log.error("Failed to describe image: {}", e.getMessage(), e);
            return "Image from document page";
        }
    }

    private String getRootCauseMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}
