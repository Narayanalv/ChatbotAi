package com.chatBot.ChatbotAi.DTO.Response;

import com.chatBot.ChatbotAi.models.ChatBot;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

public class ListBot {
    public long id;
    public String title;
    public String topic;
    public String document;
    public int chunkedData;
    public String status;
    public LocalDate CreatedDate;

    public ListBot(ChatBot chatBot) {
        this.id = chatBot.getId();
        this.title = chatBot.getTitle();
        this.topic = chatBot.getTopic();
        this.document = "https://res.cloudinary.com/dvniqmmy3/raw/upload/v1774174628/chatBotDoc/" + chatBot.getDocument();
        this.chunkedData = chatBot.getChunkedData();
        this.CreatedDate = chatBot.getCreatedDate();
        switch (chunkedData) {
            case 1:
                this.status = "Processing";
                break;
            case 2:
                this.status = "Completed";
                break;
            case 0:
                this.status = "Pending";
        }
    }
}
