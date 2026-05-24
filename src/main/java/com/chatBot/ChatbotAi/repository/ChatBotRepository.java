package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.ChatBot;
import jakarta.transaction.Transactional;
import org.hibernate.sql.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatBotRepository extends JpaRepository<ChatBot, Long> {
    public Optional<ChatBot> findByChunkedData(Integer chunkedData);

    @Modifying
    @Transactional
    @Query("UPDATE ChatBot u SET u.chunkedData = :toVal WHERE u.chunkedData = :existVal AND u.id = :id")
    public int updateChunkedData(@Param("toVal") int toVal, @Param("existVal") int existVal, @Param("id") Long id);

    @Query("SELECT EXISTS(SELECT 1 FROM ChatBot WHERE id = :chatBotId AND visible = :status AND userId = :userId)")
    public boolean existsChatBotToUserId(Long chatBotId, boolean status, Long userId);

    public Optional<ChatBot> findById(Long id);

    @Query("SELECT c FROM ChatBot c WHERE c.visible = true AND c.userId = :id")
    Optional<List<ChatBot>> getBotsByUserId(Long id);
}
