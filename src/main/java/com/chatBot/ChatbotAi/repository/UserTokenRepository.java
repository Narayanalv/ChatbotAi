package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.UserToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    @Modifying
    @Transactional
    @Query("UPDATE UserToken u SET u.active = false WHERE u.token = :token")
    public boolean logout(@Param("token") String token);
}
