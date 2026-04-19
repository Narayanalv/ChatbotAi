package com.chatBot.ChatbotAi.controller;

import com.chatBot.ChatbotAi.DTO.Request.AddChatBotRequest;
import com.chatBot.ChatbotAi.DTO.Response.ListBot;
import com.chatBot.ChatbotAi.DTO.Response.ListBotResponse;
import com.chatBot.ChatbotAi.DTO.Response.LoginResponse;
import com.chatBot.ChatbotAi.DTO.Request.VerifyOTPRequest;
import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.DTO.Request.LoginRequest;
import com.chatBot.ChatbotAi.DTO.Request.RegisterRequest;
import com.chatBot.ChatbotAi.models.*;
import com.chatBot.ChatbotAi.repository.ApiKeyRepository;
import com.chatBot.ChatbotAi.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController extends UserControllerHelper {
    private final UserService userService;
    private final OtpService otpService;
    private final UserTokenService userTokenService;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;
    private final ChatBotService chatBotService;
    private final ApiKeyRepository apiKeyRepository;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody RegisterRequest registerRequest) {
        Response response = validateRegister(registerRequest);
        if (response.getStatus() == SUCCESS_CODE) {
            try {
                User user = userService.registerUser(registerRequest);
                System.out.println(user);
                if (!this.sendOTP(user)) {
                    throw new UsernameNotFoundException("Username not found: " + registerRequest.getEmail());
                }
            } catch (Exception e) {
                response.setStatus(ERROR_CODE);
                response.setMessage("Something went wrong " + e.getMessage());
            }
        }
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping("/verifyOTP")
    public ResponseEntity<LoginResponse> verifyOTP(@RequestBody VerifyOTPRequest verifyOTP) {
        LoginResponse response = validateVerifyOTP(verifyOTP);
        String email = verifyOTP.getEmail();
        Optional<User> user = null;
        if (response.getStatus() == SUCCESS_CODE) {
            user = userService.findUserByEmail(email);
            Optional<Otp> otpData = otpService.findOtpByid(user.get().getOtpId());
            if (otpData.isPresent()) {
                int updated = otpService.updateStatus(otpData.get().getId(), otpData.get().getStatus(), Otp.StatusEnum.VERIFIED);
                if (updated == 1) {
                    user.get().setVerified(true);
                    userService.updateUser(user.get());
                    response.setAccessToken(this.generateAccessToken(user.get()));
                }
            }
        }
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (DisabledException | LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response("User Not Found", ERROR_CODE));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response("Invalid email or password.", ERROR_CODE));
        }
        User user = userService.findUserByEmail(loginRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException(loginRequest.getEmail()));
        return ResponseEntity.ok(new LoginResponse(this.generateAccessToken(user)));
    }

    @GetMapping("/isLoggedIn")
    public ResponseEntity<Response> isLoggedIn(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new Response());
    }

    @GetMapping("/logout")
    public ResponseEntity<Response> logout(@AuthenticationPrincipal User user) {
        userTokenService.logout(user.getToken());
        return ResponseEntity.ok(new Response());
    }

    @PostMapping("/addChatBot")
    public ResponseEntity<Response> addChatBot(AddChatBotRequest request, @AuthenticationPrincipal User user) throws IOException {
        MultipartFile file = request.getFile();
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        byte[] fileBytes = file.getBytes();
        PDDocument document = Loader.loadPDF(fileBytes);
        Response response = new Response();
        if (this.hasImage(document)) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Pdf should contain only text");
        } else if (request.getTitle().isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Title should not be empty");
        } else if (request.getTopic().isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Topic should not be empty");
        } else {
            System.out.println("sucees");
            URL imageName = new URL(cloudinaryService.uploadFile(fileBytes));
//            System.out.println(imageName);
            String fileName = Path.of(imageName.getPath()).getFileName().toString();
            ChatBot chatBot = chatBotService.createChat(user.getId(), request.getTitle(), request.getTopic(), fileName);
        }
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/createApiKey/{id}")
    public ResponseEntity<Response> createApiKey(@AuthenticationPrincipal User user, @PathVariable("id") Long botId) {
        ApiKey apiKey = new ApiKey();
        apiKey.setChatBotId(botId);
        apiKey.setUserId(user.getId());
        apiKey.setApiKey();
        apiKeyRepository.save(apiKey);
        return ResponseEntity.ok(new Response());
    }

    @GetMapping("/getApiKey/{id}")
    public ResponseEntity<Response> getApiKey(@AuthenticationPrincipal User user, @PathVariable("id") Long ApiId) {
        Response response = new Response();
        Optional<ApiKey> apiKey = apiKeyRepository.findById(ApiId);
        if (apiKey.isPresent()) {
            response.setMessage(apiKey.get().getApiKey());
        } else {
            response.setStatus(ERROR_CODE);
            response.setMessage("ApiKey not found");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getBots")
    public ResponseEntity<ListBotResponse> getBots(@AuthenticationPrincipal User user) {
        Optional<List<ChatBot>> chatBotData = chatBotService.getAllChatBots(user.getId());
        ListBotResponse listBotResponse;
        if (chatBotData.isPresent()) {
            List<ListBot> listBot = new java.util.ArrayList<>(List.of());
            for (ChatBot chatBot : chatBotData.get()) {
                listBot.add(new ListBot(chatBot));
            }
            listBotResponse = new ListBotResponse(listBot);
        } else {
            listBotResponse = new ListBotResponse(false);
        }
        return ResponseEntity
                .status(listBotResponse.getStatus())
                .body(listBotResponse);
    }
}
