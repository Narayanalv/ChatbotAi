package com.chatBot.ChatbotAi.controller;

import com.chatBot.ChatbotAi.DTO.Response.LoginResponse;
import com.chatBot.ChatbotAi.DTO.Request.VerifyOTPRequest;
import com.chatBot.ChatbotAi.DTO.Request.RegisterRequest;
import com.chatBot.ChatbotAi.DTO.Response.Response;
import com.chatBot.ChatbotAi.models.Otp;
import com.chatBot.ChatbotAi.models.User;
import com.chatBot.ChatbotAi.models.UserToken;
import com.chatBot.ChatbotAi.service.OtpService;
import com.chatBot.ChatbotAi.service.UserService;
import com.chatBot.ChatbotAi.service.UserTokenService;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chatBot.ChatbotAi.JWT.JwtUtils;

@Component
public class UserControllerHelper {
    @Autowired
    private UserService userService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserTokenService userTokenService;
    @Autowired
    private JwtUtils jwtUtils;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    protected static final int SUCCESS_CODE = 200;
    protected static final int ERROR_CODE = 400;

    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    protected Response validateRegister(RegisterRequest registerRequest) {
        Response response = new Response();
        String name = registerRequest.getName();
        String password = registerRequest.getPassword();
        String cPassword = registerRequest.getConfirmPassword();
        String email = registerRequest.getEmail();
        Optional<User> user = userService.findUserByEmail(email);
        if (name.isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Name and Password are required");
        } else if (password.isEmpty() || cPassword.isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Password and Confirm Password are required");
        } else if (!password.equals(cPassword)) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Passwords do not match");
        } else if (email.isEmpty()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Email is required");
        } else if (!isValidEmail(email)) {
            response.setStatus(ERROR_CODE);
            response.setMessage("Enter a valid email");
        } else if (user.isPresent() && user.get().isVerified()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("This email is registered");
        }
        return response;
    }

    protected String generateOtp(int len) {
        String numbers = "0123456789";
        Random rndm_method = new Random();
        char[] otp = new char[len];
        for (int i = 0; i < len; i++) {
            otp[i] = numbers.charAt(rndm_method.nextInt(numbers.length()));
        }
        return new String(otp);
    }

    protected boolean sendOTP(User user) {
        Otp otp = new Otp();
        otp.setEmail(user.getEmail());
        otp.setOtp(Integer.parseInt(generateOtp(6)));
        otp.setUser(user);
        System.out.println(otp.getOtp());
        otp = otpService.saveOtp(otp);
        userService.updateOtpId(user.getId(), otp.getId());
        return true;
    }

    protected String generateAccessToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        UserToken userToken = new UserToken();
        userToken.setToken(tokenValue);
        userToken.setUser(user);
        userToken.setActive(true);
        userTokenService.saveUserToken(userToken);
        user.setToken(tokenValue);
        userService.updateToken(user.getId(), tokenValue);
        return jwtUtils.generateJwtToken(tokenValue);
    }

    protected LoginResponse validateVerifyOTP(VerifyOTPRequest verifyOTPRequest) {
        LoginResponse response = new LoginResponse();
        int otp = verifyOTPRequest.getOtp();
        if (String.valueOf(otp).length() != 6) {
            return new LoginResponse(ERROR_CODE, "User not found");
        }
        String email = verifyOTPRequest.getEmail();
        Optional<User> user = userService.findUserByEmail(email);
        if (user.isEmpty()) {
            return new LoginResponse(ERROR_CODE, "User not found");
        } else if (user.get().getOtpId() == null) {
            return new LoginResponse(ERROR_CODE, "OTP not generated");
        }

        Optional<Otp> otpData = otpService.findOtpByid(user.get().getOtpId());
        if (otpData.filter(otpInfo -> !otpInfo.isExpired()).isPresent()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("OTP has expired");
        } else if (user.get().isVerified()) {
            response.setStatus(ERROR_CODE);
            response.setMessage("OTP is already verified");
        } else if (otpData.get().getStatus() != Otp.StatusEnum.PENDING) {
            response.setStatus(ERROR_CODE);
            response.setMessage("OTP has been " + otpData.get().getStatus());
        } else if (otp == otpData.get().getOtp()) {
            return response;
        } else {
            response.setStatus(ERROR_CODE);
            response.setMessage("Invalid OTP");
        }
        return response;
    }

    protected boolean hasImage(PDDocument document) throws IOException {
        boolean status = false;
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;
            for (COSName xObjectName : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(xObjectName);
                if (xObject instanceof PDImageXObject) {
                    status = true;
                }
            }
        }
        return status;
    }

}
