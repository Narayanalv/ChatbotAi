package com.chatBot.ChatbotAi.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@Entity
@DynamicUpdate
@DynamicInsert
public class RagChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private Long chatBotId;
    @Column(nullable = false)
    private Long userId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String textChunk;
    @Column(nullable = false)
    private Integer chunkIndex;
    @Transient
    @Column(columnDefinition = "vector(1024)")
    private PGvector embedding;
    private LocalDate CreatedDate = LocalDate.now();
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column
    private Timestamp updateAt;
}
