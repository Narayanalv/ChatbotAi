package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.RagChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RagChunkRepository extends JpaRepository<RagChunk, Long> {
    List<RagChunk> findByChatBotIdAndEncoded(Long chatBotId, int encoded);
    List<RagChunk> findByChatBotIdAndEncodedAndTextChunkIsNotNull(Long chatBotId, int encoded);
    List<RagChunk> findByChatBotIdAndChunkTypeAndTextChunkIsNull(Long chatBotId, String chunkType);
    boolean existsByChatBotIdAndEncoded(Long chatBotId, int encoded);
    @Query(value = """
            SELECT * FROM rag_chunk
            WHERE chat_bot_id = :chatBotId
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<RagChunk> findSimilarChunks(
            @Param("chatBotId") Long chatBotId,
            @Param("embedding") String embedding,
            @Param("topK") int topK
    );

    @Query(value = """
            SELECT * FROM rag_chunk
            WHERE chat_bot_id = :chatBotId
              AND (embedding <=> CAST(:embedding AS vector)) <= :maxDistance
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<RagChunk> findSimilarChunksWithThreshold(
            @Param("chatBotId") Long chatBotId,
            @Param("embedding") String embedding,
            @Param("topK") int topK,
            @Param("maxDistance") double maxDistance
    );

    @Modifying
    @Query(value = """
            INSERT INTO rag_chunk 
            (created_date, chat_bot_id, chunk_index, embedding, text_chunk, user_id,creation_at,update_at)
            
            VALUES (:createdDate, :chatBotId, :chunkIndex, CAST(:embedding AS vector), :textChunk, :userId,:creationAt,:updateAt)
            """, nativeQuery = true)
    void insertRagChunk(
            @Param("createdDate") LocalDate createdDate,
            @Param("chatBotId") Long chatBotId,
            @Param("chunkIndex") int chunkIndex,
            @Param("embedding") String embedding,
            @Param("textChunk") String textChunk,
            @Param("userId") Long userId,
            @Param("creationAt") Timestamp creationAt,
            @Param("updateAt") Timestamp updateAt
    );
}
