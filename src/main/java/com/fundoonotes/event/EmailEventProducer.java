package com.fundoonotes.event;

import com.fundoonotes.config.RabbitMqConfig;
import com.fundoonotes.dto.request.EmailNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(EmailNotificationEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EMAIL_EXCHANGE, RabbitMqConfig.EMAIL_ROUTING_KEY, event);
        log.info("Published email event to queue for {}", event.getTo());
    }
}
