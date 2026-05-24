package com.chatBot.ChatbotAi.JWT;

import com.chatBot.ChatbotAi.models.ApiKey;
import com.chatBot.ChatbotAi.models.ChatBot;
import com.chatBot.ChatbotAi.service.ApiKeyService;
import com.chatBot.ChatbotAi.service.ChatBotService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final ChatBotService chatBotService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only apply this filter to the /chat/ endpoint (or /chat)
        return !(path.equals("/chat/") || path.equals("/chat"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        boolean success = false;

        if (authHeader != null) {
            // Support both "Bearer <apiKey>" and raw "<apiKey>"
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            Optional<ApiKey> apiKeyData = apiKeyService.getApiKeyByApiKey(token);
            
            if (apiKeyData.isPresent()) {
                ApiKey apiKey = apiKeyData.get();
                Optional<ChatBot> chatBotOpt = chatBotService.getChatBotById(apiKey.getChatBotId());
                if (chatBotOpt.isPresent()) {
                    ChatBot chatBot = chatBotOpt.get();
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(chatBot, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    success = true;
                }
            }
        }
        if(!success){
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"message\":\"Unauthorized\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
