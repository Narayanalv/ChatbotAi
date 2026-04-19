package com.chatBot.ChatbotAi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatBotId;
    private Long userId;
    @Column(unique = true)
    private String apiKey;
    private boolean active;
    private LocalDate createdDate = LocalDate.now();
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;
    @UpdateTimestamp
    private Date updateAt;

    public ApiKey() {

    }

    public void setApiKey()
    {
        this.apiKey = generateApiKey();
    }
    public String generateApiKey(){
        return UUID.randomUUID().toString();
    }
}
