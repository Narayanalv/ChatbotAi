package com.chatBot.ChatbotAi.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenService {

    private static final String GOOGLE_CLIENT_ID = "884028107017-o8r0i2ofu1i99en6i94ulpg1q4ujno5g.apps.googleusercontent.com";

    public boolean verifyToken(String tokenFromAngular) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            // This automatically checks the signature, expiration time, and audience Issuer
            GoogleIdToken idToken = verifier.verify(tokenFromAngular);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Token is completely valid! You can now extract user profile data:
                String userId = payload.getSubject(); // Unique Google User ID
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                System.out.println("User authenticated: " + name + " (" + email + ")");
                return true;
            } else {
                System.out.println("Invalid ID Token.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error verifying Google token: " + e.getMessage());
            return false;
        }
    }
}