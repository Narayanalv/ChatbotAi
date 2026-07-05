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
import com.chatBot.ChatbotAi.models.RagChunk;
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
import org.springframework.ai.embedding.EmbeddingModel;
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
    private final EmbeddingModel embeddingModel;

    @Value("${spring.app.imageBasePath}")
    private String basePath;

    @GetMapping("/split")
    public ResponseEntity<Response> split() throws MalformedURLException {
        Optional<ChatBot> chatBot = chatBotService.getPendingChunk(0);
        Response response = new Response("No pending chatbot found with status 0", 400);
        if (chatBot.isEmpty()) {
            log.info("No chatbot in status 0 found for splitting.");
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
        }
        if (chatBotService.updateChatBotChunkStart(chatBot.get().getId()) > 0) {
            log.info("Starting split process for chatbot ID: {}", chatBot.get().getId());
            
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

                            // Save as IMAGE chunk (description is initially null)
                            i++;
                            ragChunkService.saveImageChunk(null, imageUrl, i, chatBot.get().getId(), chatBot.get().getUserId());
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

            response.setStatus(200);
            response.setMessage("Splitting success. Raw chunks saved.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/encode")
    public ResponseEntity<Response> encode() {
        long startTime = System.currentTimeMillis();
        Optional<ChatBot> chatBot = chatBotService.getPendingChunk(1);
        Response response = new Response("No pending chatbot found with status 1", 400);
        if (chatBot.isEmpty()) {
            log.info("No chatbot in status 1 found for encoding.");
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
        }

        Long chatBotId = chatBot.get().getId();
        log.info("Found chatbot in status 1, ID: {}", chatBotId);

        List<RagChunk> pendingChunks = ragChunkService.getPendingChunks(chatBotId);
        if (pendingChunks.isEmpty()) {
            log.info("No chunks found with status 0 for chatbot ID: {}. Updating status to 2.", chatBotId);
            if (chatBotService.updateChatBotChunkComplete(chatBotId) > 0) {
                response.setStatus(200);
                response.setMessage("No chunks found with status 0. Chatbot marked as fully complete (status 2).");
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        log.info("Found {} pending chunks to encode for chatbot ID: {}", pendingChunks.size(), chatBotId);
        for (RagChunk chunk : pendingChunks) {
            if (System.currentTimeMillis() - startTime >= 30000) {
                log.warn("Encoding timed out (reached 30 seconds limit). Ending execution early.");
                break;
            }
            // Set status to 1 (in-progress)
            ragChunkService.updateChunkStatus(chunk.getId(), 1);
            try {
                // Encode the text
                float[] vector = embeddingModel.embed(chunk.getTextChunk());
                // Save embedding and set status to 2 (completed)
                ragChunkService.saveChunkEmbedding(chunk.getId(), vector, 2);
                log.info("Successfully encoded and saved chunk ID: {}", chunk.getId());
            } catch (Exception e) {
                log.error("Failed to encode chunk ID: {}. Reverting status back to 0. Error: {}", chunk.getId(), e.getMessage());
                // Revert status to 0 so it can be retried
                ragChunkService.updateChunkStatus(chunk.getId(), 0);
            }
        }

        // Check if there are any remaining chunks in status 0 or 1
        if (!ragChunkService.hasPendingChunks(chatBotId)) {
            log.info("All chunks successfully encoded for chatbot ID: {}. Updating chatbot status to 2.", chatBotId);
            if (chatBotService.updateChatBotChunkComplete(chatBotId) > 0) {
                response.setStatus(200);
                response.setMessage("All chunks successfully encoded. Chatbot status updated to 2.");
            }
        } else {
            log.warn("Some chunks for chatbot ID: {} failed to encode or are still in progress.", chatBotId);
            response.setStatus(206); // Partial Content / Processing incomplete
            response.setMessage("Encoding batch processed, but some chunks are still pending.");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/describe")
    public ResponseEntity<Response> describe() {
        long startTime = System.currentTimeMillis();
        Optional<ChatBot> chatBot = chatBotService.getPendingChunk(1);
        Response response = new Response("No pending chatbot found with status 1", 400);
        if (chatBot.isEmpty()) {
            log.info("No chatbot in status 1 found for describing.");
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getStatus()));
        }

        Long chatBotId = chatBot.get().getId();
        log.info("Found chatbot in status 1, ID: {}, describing images...", chatBotId);

        List<RagChunk> pendingImageChunks = ragChunkService.getPendingImageChunks(chatBotId);
        if (pendingImageChunks.isEmpty()) {
            log.info("No pending image chunks to describe for chatbot ID: {}", chatBotId);
            response.setStatus(200);
            response.setMessage("No image chunks to describe.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        log.info("Found {} image chunks to describe for chatbot ID: {}", pendingImageChunks.size(), chatBotId);
        for (RagChunk chunk : pendingImageChunks) {
            if (System.currentTimeMillis() - startTime >= 30000) {
                log.warn("Image description timed out (reached 30 seconds limit). Ending execution early.");
                break;
            }
            try {
                log.info("Describing image chunk ID: {}", chunk.getId());
                String description = chatService.describeImage(chunk.getImageUrl());
                ragChunkService.updateChunkText(chunk.getId(), description);
                log.info("Successfully described image chunk ID: {}", chunk.getId());
            } catch (Exception e) {
                log.error("Failed to describe image chunk ID: {}. Error: {}", chunk.getId(), e.getMessage());
            }
        }

        response.setStatus(200);
        response.setMessage("Image description processing complete.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
