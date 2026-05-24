package com.chatBot.ChatbotAi.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AddChatBotRequest {
    @NotBlank(message = "title is required")
    @Size(min = 3, max = 20, message = "Title must be between 3 and 20 characters")
    public String title;
    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 20, message = "Description must be between 3 and 20 characters")
    public String topic;
    @NotNull(message = "File is required")
    public MultipartFile file;
}
