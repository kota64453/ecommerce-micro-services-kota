package com.ecommerce.order.producer;

import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:notification.exchange}")
    private String exchange;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing Order Created event: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(exchange, "order.created", event);
    }
}
