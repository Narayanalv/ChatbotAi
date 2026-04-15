package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.models.User;
import com.chatBot.ChatbotAi.models.UserToken;
import com.chatBot.ChatbotAi.repository.UserRepository;
import com.chatBot.ChatbotAi.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Token not found: " + email));
    }

    public UserToken loadUserByToken(String token) throws UsernameNotFoundException {
        return userTokenRepository.findByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("Token not found: " + token));
    }
}