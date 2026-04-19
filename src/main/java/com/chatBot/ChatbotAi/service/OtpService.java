package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.Otp;
import com.chatBot.ChatbotAi.repository.OtpRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    public int updateStatus(Long id, Otp.StatusEnum fromStatus, Otp.StatusEnum toStatus) {
        return otpRepository.updateStatus(id, fromStatus, toStatus);
    }
}
