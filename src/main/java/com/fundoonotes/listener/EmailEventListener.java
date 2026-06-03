package com.fundoonotes.listener;

import com.fundoonotes.config.RabbitMqConfig;
import com.fundoonotes.dto.request.EmailNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void consume(EmailNotificationEvent event) {
        if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)
                || "your-email@gmail.com".equalsIgnoreCase(mailUsername)
                || "your-app-password".equals(mailPassword)) {
            log.warn("Mail credentials are not configured. Skipping email send for {}", event.getTo());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getTo());
            message.setSubject(event.getSubject());
            message.setText(event.getBody());
            mailSender.send(message);
            log.info("Verification email sent to {}", event.getTo());
        } catch (Exception ex) {
            // Keep logs visible in dev to avoid dropping errors silently.
            log.error("Failed to send email to {}: {}", event.getTo(), ex.getMessage());
        }
    }
}
