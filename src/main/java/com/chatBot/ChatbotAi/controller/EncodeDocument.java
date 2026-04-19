package com.chatBot.ChatbotAi.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.models.ChatBot;
import com.chatBot.ChatbotAi.service.ChatBotService;
import com.chatBot.ChatbotAi.service.RagChunkService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EncodeDocument {
    private final ChatBotService chatBotService;
    private final RagChunkService ragChunkService;
    @Value("${spring.app.imageBasePath}")
    private String basePath;

    @GetMapping("/test")
    public ResponseEntity<Response> encode() throws MalformedURLException {
        Optional<ChatBot> chatBot = chatBotService.getPendingChunk(0);
        Response response = new Response("No data", 400);
        if (chatBot.isEmpty()) {
            System.out.println("empty chatBot");
//            System.exit(0);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
        }
        if (chatBotService.updateChatBotChunkStart(chatBot.get().getId()) > 0) {
            TikaDocumentReader reader = new TikaDocumentReader(new UrlResource(basePath + chatBot.get().getDocument()));
            String chunks = new TokenTextSplitter()
                    .apply(reader.get()).toString();
            List<String> data = Arrays.stream(chunks.split("\\n\\n"))
                    .map(String::trim)
                    .filter(chunk -> chunk.length() > 50)
                    .toList();
            int i = 0;
            for (String chunk : data) {
                i++;
                ragChunkService.saveChunck(chunk.trim(), i, chatBot.get().getId(), chatBot.get().getUserId());
            }
            if (chatBotService.updateChatBotChunkComplete(chatBot.get().getId()) > 0) {
                response.setStatus(200);
                response.setMessage("Success");
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
