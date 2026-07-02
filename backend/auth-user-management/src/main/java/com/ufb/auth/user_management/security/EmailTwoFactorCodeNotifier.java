package com.ufb.auth.user_management.security;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailTwoFactorCodeNotifier implements TwoFactorCodeNotifier {

    private final JavaMailSender mailSender;

    @Value("${ufb.mail.from}")
    private String fromAddress;

    public EmailTwoFactorCodeNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void deliver(String recipientEmail, String rawCode, Instant expiresAt) {
        String html = EmailTemplateBuilder.render(
                "Confirm it's you",
                List.of("Enter the code below to finish signing in to your UFB Consulting account.",
                        "This one-time check only happens on your first login."),
                rawCode,
                null,
                null,
                "This code expires at " + expiresAt + ". If you didn't try to sign in, you can safely ignore this email."
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("UFB Consulting — Your sign-in code");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send 2FA code email", e);
        }
    }
}
