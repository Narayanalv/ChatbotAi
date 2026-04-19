package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.UserToken;
import com.chatBot.ChatbotAi.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTokenService {
    @Autowired
    private UserTokenRepository userTokenRepository;

    public UserToken saveUserToken(UserToken userToken) {
        return userTokenRepository.save(userToken);
    }

    public Optional<UserToken> getUserToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    public boolean logout(String token) {
        return userTokenRepository.logout(token);
    }
}
