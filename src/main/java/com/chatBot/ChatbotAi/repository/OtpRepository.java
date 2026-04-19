package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.Otp;
import jakarta.transaction.Transactional;
import org.hibernate.sql.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.status = :toStatus WHERE o.id = :id and o.status = :fromStatus")
    public int updateStatus(@Param("id") Long id, @Param("fromStatus") Otp.StatusEnum fromStatus, @Param("toStatus") Otp.StatusEnum toStatus);
}
