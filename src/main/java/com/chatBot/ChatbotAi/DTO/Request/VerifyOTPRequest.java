package com.chatBot.ChatbotAi.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyOTPRequest {
    private int otp;
    private String email;
}
