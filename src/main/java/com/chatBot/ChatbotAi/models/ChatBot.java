package com.chatBot.ChatbotAi.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@Entity
@DynamicUpdate
@DynamicInsert
public class ChatBot {
    public ChatBot() {
    }

    public ChatBot(Long id, String title, String topic, String document) {
        this.userId = id;
        this.title = title;
        this.topic = topic;
        this.document = document;
        this.visible = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    public String title;
    public String topic;
    public String document;
    @Column(nullable = false, columnDefinition = "boolean default true")
    public boolean visible;
    @Column(columnDefinition = "SMALLINT")
    public int chunkedData = 0;
    private LocalDate CreatedDate = LocalDate.now();
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    @Column
    private Timestamp updateAt;
}
