package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.RagChunk;
import com.chatBot.ChatbotAi.repository.RagChunkRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RagChunkService {
    private final RagChunkRepository ragChunkRepository;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    @Transactional
    public void saveChunck(String data, int i, Long chatBotId, Long userId) {
        RagChunk ragChunk = new RagChunk();
        ragChunk.setTextChunk(data);
        ragChunk.setChunkIndex(0);
        ragChunk.setUserId(userId);
        ragChunk.setChatBotId(chatBotId);
        float[] vector = embeddingModel.embed(data);
        ragChunk.setEmbedding(vector);
        ragChunkRepository.save(ragChunk);
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        ragChunkRepository.insertRagChunk(
//                LocalDate.now(),
//                chatBotId,
//                i,
//                Arrays.toString(embeddingModel.embed(data)),
//                data,
//                userId,
//                timestamp,
//                timestamp
//        );
    }

    public List<RagChunk> search(String query, Long chatBotId, int topK) {
        float[] queryVector = embeddingModel.embed(query);
        String vectorStr = Arrays.toString(queryVector)
                .replace("[", "[")
                .replace("]", "]");
        return ragChunkRepository.findSimilarChunks(chatBotId, vectorStr, topK);
    }

}
