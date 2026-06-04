package com.chatBot.ChatbotAi.controller;

import com.chatBot.ChatbotAi.DTO.Request.*;
import com.chatBot.ChatbotAi.DTO.Response.*;
import com.chatBot.ChatbotAi.JWT.JwtUtils;
import com.chatBot.ChatbotAi.models.*;
import com.chatBot.ChatbotAi.repository.ApiKeyRepository;
import com.chatBot.ChatbotAi.service.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.formula.functions.T;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Array;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController extends UserControllerHelper {
    @Autowired
    private UserService userService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private ChatBotService chatBotService;
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private GoogleTokenService googleTokenService;
    @Autowired
    private ChatLogService chatLogService;
    @Value("${google.OAuth.key}")
    private String googleKey;

    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody @Valid RegisterRequest registerRequest) {
        Response response = this.validateRegister(registerRequest);
        if (response.getStatus() == SUCCESS_CODE) {
            try {
                User user = userService.registerUser(registerRequest);
                System.out.println(user);
                if (!this.sendOTP(user, Otp.TypeEnum.USER)) {
                    throw new UsernameNotFoundException("Username not found");
                }
            } catch (Exception e) {
                response.setStatus(ERROR_CODE);
                response.setMessage("Something went wrong " + e.getMessage());
            }
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/auth/google/register")
    public ResponseEntity<LoginResponse> googleOAuthRegister(@RequestBody @Valid OAuthRequest oAuthRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleKey))
                    .build();
            System.out.println(oAuthRequest.getToken());
            GoogleIdToken idToken = verifier.verify(oAuthRequest.getToken());
            System.out.println(idToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String userId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                Optional<User> userCheck = userService.findUserByEmail(email);
                if (userCheck.isEmpty()) {
                    User user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setGoogleId(userId);
                    user.setVerified(true);
                    user = userService.registerOAuth(user);
                    loginResponse = new LoginResponse();
                    loginResponse.setAccessToken(this.generateAccessToken(user));
                } else {
                    loginResponse = new LoginResponse(ERROR_CODE, "You have already registered");
                }
            } else {
                loginResponse = new LoginResponse(ERROR_CODE, "Authentication Failed 2");
            }
        } catch (Exception e) {

        }
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @PostMapping("/auth/google/login")
    public ResponseEntity<LoginResponse> googleOAuthLogin(@RequestBody @Valid OAuthRequest oAuthRequest) {
        LoginResponse loginResponse = new LoginResponse();
        try {
            System.out.println(googleKey);
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleKey))
                    .build();
            System.out.println(oAuthRequest.getToken());
            GoogleIdToken idToken = verifier.verify(oAuthRequest.getToken());
            System.out.println(idToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String userId = payload.getSubject();
                String email = payload.getEmail();
                Optional<User> userCheck = userService.getUserByEmailGId(email, userId);
                if (userCheck.isEmpty()) {
                    loginResponse = new LoginResponse(ERROR_CODE,
                            "You have not registered or registered with password");
                } else {
                    loginResponse = new LoginResponse();
                    loginResponse.setAccessToken(this.generateAccessToken(userCheck.get()));
                }
            } else {
                loginResponse = new LoginResponse(ERROR_CODE, "Authentication Failed 3");
            }
        } catch (Exception e) {

        }
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @PostMapping("/verifyOTP")
    public ResponseEntity<LoginResponse> verifyOTP(@RequestBody @Valid VerifyOTPRequest verifyOTP) {
        LoginResponse response = validateVerifyOTP(verifyOTP);
        String email = verifyOTP.getEmail();
        Optional<User> user = null;
        if (response.getStatus() == SUCCESS_CODE) {
            user = userService.findUserByEmail(email);
            Optional<Otp> otpData = otpService.findOtpByid(user.get().getOtpId());
            if (otpData.isPresent()) {
                int updated = otpService.updateStatus(otpData.get().getId(), Otp.StatusEnum.VERIFIED);
                if (updated == 1) {
                    user.get().setVerified(true);
                    userService.updateUser(user.get());
                    response.setAccessToken(this.generateAccessToken(user.get()));
                }
            }
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response("Invalid email or password.", ERROR_CODE));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response("User Not Found", ERROR_CODE));
        }
        User user = userService.findUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(loginRequest.getEmail()));
        return ResponseEntity.ok(new LoginResponse(this.generateAccessToken(user)));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid ForgetPassword forgetPassword) {
        Response response = new LoginResponse(ERROR_CODE, "User Not Found");
        try {
            Optional<User> user = userService.findUserByEmail(forgetPassword.getEmail());
            System.out.println(forgetPassword.getEmail() + " /n" + user);
            if (user.isPresent()) {
                if (this.sendOTP(user.get(), Otp.TypeEnum.RESET)) {
                    response = new Response();
                } else {
                    response.setMessage("Failed to send OTP");
                }
            }
        } catch (Exception e) {
            response = new LoginResponse(ERROR_CODE, "Something went wrong: " + e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verifyForgot")
    public ResponseEntity<LoginResponse> verifyForgot(@RequestBody @Valid VerifyOTPRequest verifyOTP) {
        LoginResponse response = new LoginResponse(ERROR_CODE, "User Not Found");
        Optional<User> user = userService.findUserByEmail(verifyOTP.getEmail());
        System.out.println(verifyOTP.getEmail() + " /n" + user.get().getId());
        if (user.isPresent()) {
            Optional<Otp> otp = otpService.findOtpByid(user.get().getOtpId());
            if (otp.isEmpty()) {
                response.setMessage("Invalid Otp");
            } else if (otp.get().isExpired()) {
                response.setMessage("Expired Otp");
            } else if (String.valueOf(otp.get().getOtp()).equals(verifyOTP.getOtp())) {
                int updated = otpService.updateStatus(otp.get().getId(), Otp.StatusEnum.VERIFIED);
                if (updated == 1) {
                    user.get().setVerified(true);
                    userService.updateUser(user.get());
                    response = new LoginResponse(this.generateResetAccessToken(user.get()));
                } else {
                    response.setMessage("Could not verify your Otp");
                }
            } else {
                response.setMessage("Not equal");
            }
        } else {
            response.setMessage("user empty");
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Response> changePassword(@RequestBody @Valid ChangePassword changePassword,
            @NonNull HttpServletRequest request) {
        Response response = new LoginResponse(ERROR_CODE, "User Not Found");
        String accessToken = jwtUtils.getAuthenticationToken(request);
        String token = jwtUtils.getSessionFromJwtToken(accessToken);
        Optional<UserToken> userToken = userTokenService.getUserToken(token);
        if (jwtUtils.validateJwtToken(accessToken) && userToken.isPresent()) {
            Claims claim = jwtUtils.getClaims(accessToken);
            String app = claim.get("app").toString();
            User user = userToken.get().getUser();
            if (!app.equals("resetPassword")) {
                response.setStatus(401);
                response.setMessage("Unauthorized");
            } else if (changePassword.getPassword().equals(changePassword.getConfirmPassword())) {
                int update = userService.updatePassword(user.getId(), changePassword.getPassword());
                if (update == 1) {
                    response = new Response();
                }
            }
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setMessage("Unauthorized");
        }
        return ResponseEntity.status(response.getStatus()).body(response);
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
    public ResponseEntity<Response> addChatBot(@ModelAttribute @Valid AddChatBotRequest request,
            @AuthenticationPrincipal User user) throws IOException {
        MultipartFile file = request.getFile();
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        System.out.println("check");
        System.out.println("Uploading file: " + file.getOriginalFilename());
        byte[] fileBytes = file.getBytes();
        Response response = new Response();
        if (request.getTitle().isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Title should not be empty");
        } else if (request.getTopic().isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Topic should not be empty");
        } else {
            System.out.println("sucees");
            URL imageName = new URL(cloudinaryService.uploadFile(fileBytes));
            // System.out.println(imageName);
            String fileName = Path.of(imageName.getPath()).getFileName().toString();
            ChatBot chatBot = chatBotService.createChat(user.getId(), request.getTitle(), request.getTopic(), fileName);
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/createApiKey/{id}")
    public ResponseEntity<Response> createApiKey(@AuthenticationPrincipal User user, @PathVariable("id") Long botId) {
        Optional<ApiKey> apikeyExist = apiKeyRepository.getVisibleApikey(botId, true);
        if (apikeyExist.isPresent()) {
            return ResponseEntity.ok(new Response("api Key is already created", ERROR_CODE));
        }
        ApiKey apiKey = new ApiKey();
        apiKey.setChatBotId(botId);
        apiKey.setUserId(user.getId());
        apiKey.setApiKey();
        apiKeyRepository.save(apiKey);
        return ResponseEntity.ok(new Response("created api key successfully", SUCCESS_CODE));
    }

    @GetMapping("/getApiKey/{id}")
    public ResponseEntity<ApiKeyResponse> getApiKey(@AuthenticationPrincipal User user,
            @PathVariable("id") Long apiId) {
        Optional<ApiKey> apiKeyList = apiKeyRepository.getVisibleApikey(apiId, true);
        ApiKeyResponse response = new ApiKeyResponse();
        ApiKeyList listApiKey = new ApiKeyList();
        if (apiKeyList.isPresent()) {
            listApiKey = new ApiKeyList(apiKeyList.get());
            response.setApiKeyList(listApiKey);
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/activateKey/{id}/{status}")
    public ResponseEntity<Response> activateApiKey(@AuthenticationPrincipal User user,
            @PathVariable("id") Long apiKeyId, @PathVariable("status") boolean status) {
        Response response = new Response();
        int apiUpdate = apiKeyRepository.updateApiKeyStatus(apiKeyId, status, !status);
        if (apiUpdate >= 1) {
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            response.setStatus(ERROR_CODE);
            response.setMessage("failed to activate api key");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
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
        return ResponseEntity.status(listBotResponse.getStatus()).body(listBotResponse);
    }

    @GetMapping("/getKey/{id}")
    public ResponseEntity<GetApiKeyResponse> getKey(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        Optional<ApiKey> key = apiKeyRepository.findById(id);
        GetApiKeyResponse getApiKeyResponse = new GetApiKeyResponse();
        if (key.isPresent()) {
            getApiKeyResponse.setApiKey(key.get().getApiKey());
            return ResponseEntity.ok(getApiKeyResponse);
        } else {
            getApiKeyResponse.setApiKey(null);
            return ResponseEntity.status(ERROR_CODE).body(getApiKeyResponse);
        }
    }

    @GetMapping("/deleteApiKey/{id}")
    public ResponseEntity<Response> deleteApiKey(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        Optional<ApiKey> key = apiKeyRepository.findById(id);
        GetApiKeyResponse getApiKeyResponse = new GetApiKeyResponse();
        if (key.isPresent()) {
            int deleted = apiKeyRepository.updateVisible(id, false);
            if (deleted >= 1) {
                return ResponseEntity.ok(new Response("deleted api key successfully", SUCCESS_CODE));
            } else {
                return ResponseEntity.status(ERROR_CODE).body(new Response("Failed to delete", ERROR_CODE));
            }
        } else {
            return ResponseEntity.status(401).body(new Response("Failed to delete", 401));
        }
    }

    @GetMapping("/getBot/history/{id}")
    public ResponseEntity<Response> getBotHistory(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        boolean exists = chatBotService.existsChatBotToUserId(id, user.getId());
        if (!exists) {
            return ResponseEntity.status(401).body(new Response("Unauthorized", 401));
        }

        org.springframework.data.domain.Page<com.chatBot.ChatbotAi.models.ChatLog> chatLogs = chatLogService
                .getHistoryByChatBotId(id, page, size);

        HistoryResponse historyResponse = new HistoryResponse();
        historyResponse.setStatus(SUCCESS_CODE);
        historyResponse.setMessage("success");
        historyResponse.setCurrentPage(chatLogs.getNumber());
        historyResponse.setTotalPages(chatLogs.getTotalPages());
        historyResponse.setTotalItems(chatLogs.getTotalElements());

        List<HistoryResponse.ChatHistoryItem> historyItems = chatLogs.getContent().stream().map(log -> {
            HistoryResponse.ChatHistoryItem item = new HistoryResponse.ChatHistoryItem();
            item.setId(log.getId());
            item.setMessage(log.getMessage());
            item.setResponseMessage(log.getResponseMessage());
            item.setCreatedAt(log.getCreatedAt());
            return item;
        }).toList();

        historyResponse.setHistory(historyItems);
        return ResponseEntity.ok(historyResponse);
    }
}
