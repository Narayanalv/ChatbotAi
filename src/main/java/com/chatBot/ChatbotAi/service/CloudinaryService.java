package com.chatBot.ChatbotAi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {
    @Autowired
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(byte[] file) throws IOException {
        Map options = ObjectUtils.asMap("folder", "chatBotDoc", "public_id", UUID.randomUUID().toString() + ".pdf", "upload_preset", "chatbot_unsigned", "resource_type", "raw");
        Map uploadResult = cloudinary.uploader().upload(file, options);
        return (String) uploadResult.get("url");
    }

    public String uploadImage(byte[] imageBytes, String format) throws IOException {
        Map options = ObjectUtils.asMap(
                "folder", "chatBotImages",
                "public_id", UUID.randomUUID().toString(),
                "upload_preset", "chatbot_unsigned",
                "resource_type", "image",
                "format", format
        );
        Map uploadResult = cloudinary.uploader().upload(imageBytes, options);
        return (String) uploadResult.get("url");
    }
}
