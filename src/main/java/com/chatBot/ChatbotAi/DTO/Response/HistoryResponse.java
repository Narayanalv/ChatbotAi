package com.chatBot.ChatbotAi.DTO.Response;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
public class HistoryResponse extends Response {
    private List<ChatHistoryItem> history;
    private int currentPage;
    private int totalPages;
    private long totalItems;

    @Setter
    @Getter
    public static class ChatHistoryItem {
        private Long id;
        private String message;
        private String responseMessage;
        private Timestamp createdAt;
    }
}
