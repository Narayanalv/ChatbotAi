package com.chatBot.ChatbotAi.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.models.ChatBot;
import com.chatBot.ChatbotAi.service.ChatBotService;
import com.chatBot.ChatbotAi.service.ChatService;
import com.chatBot.ChatbotAi.service.CloudinaryService;
import com.chatBot.ChatbotAi.service.RagChunkService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EncodeDocument {
    private static final Logger log = LoggerFactory.getLogger(EncodeDocument.class);
    private final ChatBotService chatBotService;
    private final RagChunkService ragChunkService;
    private final CloudinaryService cloudinaryService;
    private final ChatService chatService;
    @Value("${spring.app.imageBasePath}")
    private String basePath;

    @GetMapping("/test")
    public ResponseEntity<Response> encode() throws MalformedURLException {
        Optional<ChatBot> chatBot = chatBotService.getPendingChunk(0);
        Response response = new Response("No data", 400);
        if (chatBot.isEmpty()) {
            System.out.println("empty chatBot");
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
        }
        if (chatBotService.updateChatBotChunkStart(chatBot.get().getId()) > 0) {
            // --- TEXT CHUNKING (existing logic) ---
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

            // --- IMAGE CHUNKING (render each page as image) ---
            try {
                String pdfUrl = basePath + chatBot.get().getDocument();
                try (InputStream pdfStream = URI.create(pdfUrl).toURL().openStream()) {
                    byte[] pdfBytes = pdfStream.readAllBytes();
                    PDDocument pdfDoc = Loader.loadPDF(pdfBytes);
                    PDFRenderer pdfRenderer = new PDFRenderer(pdfDoc);
                    int totalPages = pdfDoc.getNumberOfPages();
                    log.info("PDF has {} pages, rendering each as image...", totalPages);

                    for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                        try {
                            // Render page at 150 DPI (good balance of quality vs size)
                            BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageNum, 150);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(pageImage, "png", baos);
                            byte[] imageBytes = baos.toByteArray();

                            // Upload to Cloudinary
                            String imageUrl = cloudinaryService.uploadImage(imageBytes, "png");
                            log.info("Page {} uploaded to Cloudinary: {}", pageNum + 1, imageUrl);

                            // Describe image using Groq vision
                            String description = chatService.describeImage(imageUrl);
                            log.info("Page {} described (length={})", pageNum + 1, description.length());

                            // Save as IMAGE chunk
                            i++;
                            ragChunkService.saveImageChunk(description, imageUrl, i, chatBot.get().getId(), chatBot.get().getUserId());
                            log.info("Page {} saved as IMAGE chunk index {}", pageNum + 1, i);
                        } catch (Exception e) {
                            log.error("Failed to process page {}: {}", pageNum + 1, e.getMessage(), e);
                        }
                    }
                    pdfDoc.close();
                }
            } catch (IOException e) {
                log.error("Failed to process PDF images: {}", e.getMessage(), e);
            }

            if (chatBotService.updateChatBotChunkComplete(chatBot.get().getId()) > 0) {
                response.setStatus(200);
                response.setMessage("Success");
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
