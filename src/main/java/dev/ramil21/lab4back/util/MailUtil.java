package dev.ramil21.lab4back.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class MailUtil {
    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender mailSender;

    public MailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);
            mailMessage.setFrom(from);

            mailSender.send(mailMessage);
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
