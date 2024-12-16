package dev.ramil21.lab4back.util.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailUtil {
    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;

    @Autowired
    public MailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMessage(String to, String subject, String body) {
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

    /**
     * @param to
     * @param subject
     * @param body    - html body of message
     */
    public void sendHtmlMessage(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(from);

            mailSender.send(mimeMessage);
            System.out.println("HTML Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }


}
