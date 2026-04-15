package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.Otp;
import com.chatBot.ChatbotAi.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;

    public Otp saveOtp(Otp otp) {
        return otpRepository.save(otp);
    }

    public Optional<Otp> findOtpByid(Long id) {
        return otpRepository.findById(id);
    }
}
