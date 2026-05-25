package com.chatBot.ChatbotAi.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendOtpEmail(String toEmail, String name, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("ragchatbotac@gmail.com", "ChatbotAI Support");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP");
            helper.setText(this.getBody(name, otp), true);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected String getBody(String name, String otp) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Verify Your AI Chatbot Account</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 0;-webkit-font-smoothing: antialiased;\">\n" +
                "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #f8fafc; padding: 40px 0;\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\">\n" +
                "                <table role=\"presentation\" width=\"100%\" style=\"max-width: 520px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03); overflow: hidden; border: 1px solid #e2e8f0;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                    \n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #ffffff; padding: 40px 30px 20px 30px; text-align: center;\">\n" +
                "                            <img src=\"https://placehold.co/180x45/4f46e5/ffffff?text=AI+Chatbot\" \n" +
                "                                 alt=\"AI Chatbot Logo\" \n" +
                "                                 width=\"180\" \n" +
                "                                 height=\"45\" \n" +
                "                                 style=\"display: inline-block; border: 0; max-width: 100%; height: auto; outline: none; text-decoration: none;\" />\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 20px 40px 40px 40px; color: #334155;\">\n" +
                "                            <h2 style=\"margin-top: 0; font-size: 22px; color: #0f172a; font-weight: 700; text-align: center; letter-spacing: -0.5px;\">\n" +
                "                                Security Verification Code\n" +
                "                            </h2>\n" +
                "                            \n" +
                "                            <p style=\"font-size: 15px; line-height: 1.6; color: #475569; margin-top: 20px;\">\n" +
                "                                Hello <strong>" + name + "</strong>,\n" +
                "                            </p>\n" +
                "                            \n" +
                "                            <p style=\"font-size: 15px; line-height: 1.6; color: #475569;\">\n" +
                "                                You are receiving this email to verify your identity for the <strong>Intelligent AI Chatbot System</strong>. Use the security code below to complete your authentication:\n" +
                "                            </p>\n" +
                "                            \n" +
                "                            <div style=\"text-align: center; margin: 35px 0;\">\n" +
                "                                <div style=\"display: inline-block; background-color: #f1f5f9; color: #4f46e5; font-size: 38px; font-weight: 800; letter-spacing: 8px; padding: 16px 36px; border-radius: 8px; border: 1px solid #cbd5e1; font-family: monospace;\">\n" +
                "                                    " + otp + "\n" +
                "                                </div>\n" +
                "                                <p style=\"font-size: 13px; color: #64748b; margin-top: 10px; margin-bottom: 0;\">\n" +
                "                                    This code expires in <span style=\"color: #ef4444; font-weight: 600;\">10 minutes</span>.\n" +
                "                                </p>\n" +
                "                            </div>\n" +
                "\n" +
                "                            <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #fdf2f8; border-left: 4px solid #ec4899; border-radius: 4px; margin-bottom: 30px;\">\n" +
                "                                <tr>\n" +
                "                                    <td style=\"padding: 12px 16px; font-size: 13px; line-height: 1.5; color: #9d174d;\">\n" +
                "                                        <strong>Important:</strong> For security reasons, never share this verification code with anyone, including our support agents.\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                            \n" +
                "                            <hr style=\"border: 0; border-top: 1px solid #e2e8f0; margin-bottom: 25px;\">\n" +
                "                            \n" +
                "                            <p style=\"font-size: 12px; color: #94a3b8; line-height: 1.6; margin: 0; text-align: center;\">\n" +
                "                                If you did not initiate this request to log into the AI Chatbot System, please ignore this email or contact support if you suspect unauthorized access.\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8fafc; padding: 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0;\">\n" +
                "                            <strong>Intelligent AI Chatbot System</strong><br>\n" +
                "                            &copy; 2026 Intelligent AI Chatbot System. All rights reserved.<br>\n" +
                "                            <span style=\"display: inline-block; margin-top: 6px; font-size: 11px;\">Powered by Retrieval-Guided Response Technology</span>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    \n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }
}
