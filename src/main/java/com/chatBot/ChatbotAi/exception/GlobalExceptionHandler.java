package com.chatBot.ChatbotAi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.chatBot.ChatbotAi.DTO.Response.Response;

import javax.naming.AuthenticationException;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles file upload size limit exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Response> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Response response = new Response();
        response.setStatus(400);
        response.setMessage("File size exceeds the limit of 5MB");
        return ResponseEntity.badRequest().body(response);
    }

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
        // Extract the root cause for more useful error messages
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        Response response = new Response();
        response.setStatus(500);
        response.setMessage("An unexpected error occurred: " + root.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");
        Response response = new Response();
        response.setStatus(400);
        response.setMessage(message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleValidation(
            BindException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();
        Response response = new Response();
        response.setStatus(400);
        response.setMessage(message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Response> handleIOException(IOException ex) {
        Response response = new Response();
        response.setStatus(500);
        response.setMessage("An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
