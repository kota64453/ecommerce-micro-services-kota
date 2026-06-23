package com.ecommerce.notification.consumer;

import com.ecommerce.notification.event.*;
import com.ecommerce.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "otp.queue")
    public void handleOtpEvent(OtpEvent event) {
        log.info("Received OTP event for email: {}", event.getEmail());
        emailService.sendOtpEmail(event.getEmail(), event.getFirstName(), event.getOtp());
    }

    @RabbitListener(queues = "welcome.queue")
    public void handleWelcomeEvent(WelcomeEvent event) {
        log.info("Received Welcome event for email: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getFirstName(), event.getLastName());
    }

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received Order Created event: {}", event.getOrderNumber());
        emailService.sendOrderConfirmationEmail(
                event.getEmail(),
                event.getFirstName(),
                event.getOrderNumber(),
                event.getTotalAmount().toString()
        );
    }

    @RabbitListener(queues = "payment.success.queue")
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        log.info("Received Payment Success event: {}", event.getPaymentReference());
        emailService.sendPaymentSuccessEmail(
                event.getEmail(),
                event.getFirstName(),
                event.getOrderNumber(),
                event.getAmount().toString(),
                event.getPaymentReference()
        );
    }

    @RabbitListener(queues = "forgot.password.queue")
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        log.info("Received Forgot Password event for email: {}", event.getEmail());
        emailService.sendPasswordResetEmail(event.getEmail(), event.getFirstName(), event.getOtp());
    }
}
