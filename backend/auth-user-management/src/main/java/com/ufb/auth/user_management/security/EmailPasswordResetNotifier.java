package com.ufb.auth.user_management.security;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailPasswordResetNotifier implements PasswordResetNotifier {

    private final JavaMailSender mailSender;

    @Value("${ufb.mail.from}")
    private String fromAddress;

    @Value("${ufb.frontend.base-url}")
    private String frontendBaseUrl;

    public EmailPasswordResetNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void deliver(String recipientEmail, String rawResetToken, Instant expiresAt) {
        String encodedEmail = URLEncoder.encode(recipientEmail, StandardCharsets.UTF_8);
        String encodedToken = URLEncoder.encode(rawResetToken, StandardCharsets.UTF_8);
        String resetLink = frontendBaseUrl + "/reset-password?email=" + encodedEmail + "&token=" + encodedToken;

        String html = EmailTemplateBuilder.render(
                "Reset your password",
                List.of("We received a request to reset the password on your UFB Consulting account."),
                null,
                "Reset password",
                resetLink,
                "This link expires at " + expiresAt + ". If you didn't request this, you can safely ignore this email — your password will not change."
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("UFB Consulting — Reset your password");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send password reset email", e);
        }
    }
}
