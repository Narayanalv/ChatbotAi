package com.chatBot.ChatbotAi.DTO.Response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response {
    private String message = "success";
    private Integer status = 200;
    public Response() {
    }
    public Response(String message, Integer status) {
        this.message = message;
        this.status = status;
    }
}
