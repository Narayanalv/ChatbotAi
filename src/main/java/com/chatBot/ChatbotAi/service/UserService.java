package com.chatBot.ChatbotAi.service;

import com.chatBot.ChatbotAi.DTO.Request.RegisterRequest;
import com.chatBot.ChatbotAi.models.User;
import com.chatBot.ChatbotAi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.java.Log;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    public User getUserById(Long id) {
//        return userRepository.findById(id).orElse(null);
//    }

    public User registerUser(RegisterRequest user) {
        User newUser = findUserByEmail(user.getEmail()).orElse(new User());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setName(user.getName());
        return userRepository.save(newUser);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void updateOtpId(Long userId, Long otpId) {
        userRepository.updateOtpId(userId, otpId);
    }

    public void updateToken(Long userId, String token) {
        userRepository.updateToken(userId, token);

    }

    public int updatePassword(Long userId, String password) {
        return userRepository.updatePassword(userId, passwordEncoder.encode(password));
    }

//    public void deleteUser(Long id) {
//        userRepository.deleteById(id);
//    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
