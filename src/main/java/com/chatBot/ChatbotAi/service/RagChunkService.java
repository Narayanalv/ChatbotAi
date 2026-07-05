package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.RagChunk;
import com.chatBot.ChatbotAi.repository.RagChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RagChunkService {

    @Value("${rag.search.top-k:15}")
    private int defaultTopK;

    @Value("${rag.search.max-distance:0.8}")
    private double defaultMaxDistance;
    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Transactional
    public void saveChunck(String data, int i, Long chatBotId, Long userId) {
        RagChunk ragChunk = new RagChunk();
        ragChunk.setTextChunk(data);
        ragChunk.setChunkIndex(i);
        ragChunk.setUserId(userId);
        ragChunk.setChatBotId(chatBotId);
        ragChunk.setEncoded(0); // 0 = raw/pending
        ragChunkRepository.save(ragChunk);
    }

    public List<RagChunk> search(String query, Long chatBotId, int topK) {
        float[] queryVector = embeddingModel.embed(query);
        String vectorStr = Arrays.toString(queryVector);
        return ragChunkRepository.findSimilarChunks(chatBotId, vectorStr, topK);
    }

    public List<RagChunk> searchWithThreshold(String query, Long chatBotId) {
        float[] queryVector = embeddingModel.embed(query);
        String vectorStr = Arrays.toString(queryVector);
        return ragChunkRepository.findSimilarChunksWithThreshold(chatBotId, vectorStr, defaultTopK, defaultMaxDistance);
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    @Transactional
    public void saveImageChunk(String description, String imageUrl, int i, Long chatBotId, Long userId) {
        RagChunk ragChunk = new RagChunk();
        ragChunk.setTextChunk(description);
        ragChunk.setChunkIndex(i);
        ragChunk.setUserId(userId);
        ragChunk.setChatBotId(chatBotId);
        ragChunk.setChunkType("IMAGE");
        ragChunk.setImageUrl(imageUrl);
        ragChunk.setEncoded(0); // 0 = raw/pending
        ragChunkRepository.save(ragChunk);
    }

    public List<RagChunk> getPendingChunks(Long chatBotId) {
        return ragChunkRepository.findByChatBotIdAndEncoded(chatBotId, 0);
    }

    @Transactional
    public void updateChunkStatus(Long chunkId, int status) {
        RagChunk chunk = ragChunkRepository.findById(chunkId).orElse(null);
        if (chunk != null) {
            chunk.setEncoded(status);
            ragChunkRepository.save(chunk);
        }
    }

    @Transactional
    public void saveChunkEmbedding(Long chunkId, float[] embedding, int status) {
        RagChunk chunk = ragChunkRepository.findById(chunkId).orElse(null);
        if (chunk != null) {
            chunk.setEmbedding(embedding);
            chunk.setEncoded(status);
            ragChunkRepository.save(chunk);
        }
    }

    public boolean hasPendingChunks(Long chatBotId) {
        return ragChunkRepository.existsByChatBotIdAndEncoded(chatBotId, 0) ||
               ragChunkRepository.existsByChatBotIdAndEncoded(chatBotId, 1);
    }
}
