package com.chatBot.ChatbotAi.DTO.Request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AddChatBotRequest {
    public String title;
    public String topic;
    public MultipartFile file;
}
