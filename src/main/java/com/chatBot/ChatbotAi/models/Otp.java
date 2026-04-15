package com.chatBot.ChatbotAi.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Setter
@Getter
@DynamicUpdate
@DynamicInsert
@Entity
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int otp;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    private String email;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, VERIFIED, EXPIRED, FAILED
    }

    private LocalDate CreatedDate = LocalDate.now();
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column
    private Timestamp updateAt;

    public boolean isExpired() {
        Instant tenMinutesFromNow = Instant.now().plus(10, ChronoUnit.MINUTES);
        return tenMinutesFromNow.isAfter(this.createdAt.toInstant());
    }
}
