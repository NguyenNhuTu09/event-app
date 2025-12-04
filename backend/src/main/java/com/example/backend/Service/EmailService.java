package com.example.backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender; 

    public String sendSimpleMail(String recipient, String msgBody, String subject) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender); 
            mailMessage.setTo(recipient); 
            mailMessage.setText(msgBody); 
            mailMessage.setSubject(subject); 

            javaMailSender.send(mailMessage);
            return "Gửi mail thành công!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gửi mail: " + e.getMessage();
        }
    }

    public void sendEmailWithTemplate(String to, String subject, String username, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            
            Context context = new Context();
            context.setVariable("username", username); 
            context.setVariable("otpCode", otp);       
            
            String htmlContent = templateEngine.process("otp-email", context);
            
            helper.setText(htmlContent, true); 
            javaMailSender.send(message);
            
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationEmail(String to, String subject, String username) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            
            Context context = new Context();
            context.setVariable("username", username);
            
            // Trỏ tới file password-changed.html
            String htmlContent = templateEngine.process("password-changed", context);
            
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {    
            e.printStackTrace();
        }
    }

}
