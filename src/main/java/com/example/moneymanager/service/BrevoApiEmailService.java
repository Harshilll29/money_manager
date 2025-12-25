package com.example.moneymanager.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
public class BrevoApiEmailService {

    private final HttpMessageConverters messageConverters;
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:Money Manager}")
    private String senderName;

    private final OkHttpClient client = new OkHttpClient();

    public BrevoApiEmailService(HttpMessageConverters messageConverters) {
        this.messageConverters = messageConverters;
    }

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


public void sendEmailWithAttachment(String to, String subject, String htmlBody, String filename, byte[] attachment) {
    try {
        // Escape quotes in HTML body for JSON
        String escapedBody = htmlBody
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");

        // Convert attachment to Base64
        String base64Attachment = Base64.getEncoder().encodeToString(attachment);

        // Build JSON with attachment
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
                  "htmlContent": "%s",
                  "attachment": [
                    {
                      "content": "%s",
                      "name": "%s"
                    }
                  ]
                }
                """, senderName, senderEmail, to, subject, escapedBody, base64Attachment, filename);

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
                throw new RuntimeException("Failed to send email with attachment via Brevo API: " + errorBody);
            }
            log.info("Email with attachment sent successfully to: {}", to);
        }
    } catch (Exception e) {
        log.error("Error sending email with attachment: {}", e.getMessage(), e);
        throw new RuntimeException("Email sending with attachment failed: " + e.getMessage(), e);
    }
}
}