package com.ecommerce.auth.producer;

import com.ecommerce.auth.event.ForgotPasswordEvent;
import com.ecommerce.auth.event.OtpEvent;
import com.ecommerce.auth.event.WelcomeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:notification.exchange}")
    private String exchange;

    public void publishOtpEvent(OtpEvent event) {
        log.info("Publishing OTP event for email: {}", event.getEmail());
        rabbitTemplate.convertAndSend(exchange, "otp.send", event);
    }

    public void publishWelcomeEvent(WelcomeEvent event) {
        log.info("Publishing Welcome event for email: {}", event.getEmail());
        rabbitTemplate.convertAndSend(exchange, "welcome.send", event);
    }

    public void publishForgotPasswordEvent(ForgotPasswordEvent event) {
        log.info("Publishing Forgot Password event for email: {}", event.getEmail());
        rabbitTemplate.convertAndSend(exchange, "password.reset", event);
    }
}
