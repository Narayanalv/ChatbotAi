package com.chatBot.ChatbotAi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.chatBot.ChatbotAi.DTO.Response.Response;

import javax.naming.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles Authentication Failures (Login errors)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response> handleAuthenticationException(AuthenticationException ex) {
        Response response = new Response();
        response.setStatus(401);
        response.setMessage("Invalid email or password");
        // Returning an empty map to match your DTO's data field
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGeneralException(Exception ex) {
        Response response = new Response();
        response.setStatus(500);
        response.setMessage("An unexpected error occurred: " + ex);
        return ResponseEntity.internalServerError().body(response);
    }
}
