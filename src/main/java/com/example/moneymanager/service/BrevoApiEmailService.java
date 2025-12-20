//package com.example.moneymanager.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.from}")
//    private String fromEmail;
//
////    public void sendEmail(String to, String subject, String body){
////        try {
////            SimpleMailMessage message = new SimpleMailMessage();
////            message.setFrom(fromEmail);
////            message.setTo(to);
////            message.setSubject(subject);
////            message.setText(body);
////            mailSender.send(message);
////        }catch(Exception e){
////            throw new RuntimeException(e.getMessage());
////        }
////    }
//
//    public void sendEmail(String to, String subject, String body){
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(body, true);
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
//        }
//    }
//
//}
//
//


package com.example.moneymanager.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BrevoApiEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:Money Manager}")
    private String senderName;

    private final OkHttpClient client = new OkHttpClient();

    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            // Escape quotes in HTML body for JSON
            String escapedBody = htmlBody
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String json = String.format("""
                {
                  "sender": {
                    "name": "%s",
                    "email": "%s"
                  },
                  "to": [
                    {
                      "email": "%s"
                    }
                  ],
                  "subject": "%s",
                  "htmlContent": "%s"
                }
                """, senderName, senderEmail, to, subject, escapedBody);

            RequestBody body = RequestBody.create(
                    json,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://api.brevo.com/v3/smtp/email")
                    .addHeader("accept", "application/json")
                    .addHeader("api-key", apiKey)
                    .addHeader("content-type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    log.error("Brevo API error: {} - {}", response.code(), errorBody);
                    throw new RuntimeException("Failed to send email via Brevo API: " + errorBody);
                }
                log.info("Email sent successfully to: {}", to);
            }
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}