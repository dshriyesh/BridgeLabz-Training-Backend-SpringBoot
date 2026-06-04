package com.fundoonotes.listener;

import com.fundoonotes.config.RabbitMqConfig;
import com.fundoonotes.dto.request.EmailNotificationEvent;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class EmailEventListener {

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @Value("${SENDGRID_FROM_EMAIL:}")
    private String fromEmail;

    @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
    public void consume(EmailNotificationEvent event) {
        if (sendGridApiKey.isBlank() || fromEmail.isBlank()) {
            log.warn("SendGrid credentials not configured. Skipping email for {}", event.getTo());
            return;
        }
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(event.getTo());
            Content content = new Content("text/plain", event.getBody());
            Mail mail = new Mail(from, event.getSubject(), to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent to {} via SendGrid HTTP API", event.getTo());
            } else {
                log.error("SendGrid error: {} - {}", response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            log.error("Failed to send email to {}: {}", event.getTo(), ex.getMessage());
        }
    }
}



// package com.fundoonotes.listener;

// import com.fundoonotes.config.RabbitMqConfig;
// import com.fundoonotes.dto.request.EmailNotificationEvent;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Component;
// import org.springframework.util.StringUtils;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class EmailEventListener {

//     private final JavaMailSender mailSender;

//     @Value("${spring.mail.username:}")
//     private String mailUsername;

//     @Value("${spring.mail.password:}")
//     private String mailPassword;

//     @RabbitListener(queues = RabbitMqConfig.EMAIL_QUEUE)
//     public void consume(EmailNotificationEvent event) {
//         if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)
//                 || "your-email@gmail.com".equalsIgnoreCase(mailUsername)
//                 || "your-app-password".equals(mailPassword)) {
//             log.warn("Mail credentials are not configured. Skipping email send for {}", event.getTo());
//             return;
//         }
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(event.getTo());
//             message.setSubject(event.getSubject());
//             message.setText(event.getBody());
//             mailSender.send(message);
//             log.info("Verification email sent to {}", event.getTo());
//         } catch (Exception ex) {
//             // Keep logs visible in dev to avoid dropping errors silently.
//             log.error("Failed to send email to {}: {}", event.getTo(), ex.getMessage());
//         }
//     }
// }
