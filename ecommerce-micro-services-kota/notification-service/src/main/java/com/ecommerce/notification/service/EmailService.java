package com.ecommerce.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpEmail(String to, String firstName, String otp) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("expiryMinutes", 5);

            String htmlContent = templateEngine.process("otp-email", context);
            sendEmail(to, "Your OTP for E-Commerce App", htmlContent);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    public void sendWelcomeEmail(String to, String firstName, String lastName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);

            String htmlContent = templateEngine.process("welcome-email", context);
            sendEmail(to, "Welcome to E-Commerce App!", htmlContent);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendOrderConfirmationEmail(String to, String firstName, String orderNumber, String totalAmount) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("orderNumber", orderNumber);
            context.setVariable("totalAmount", totalAmount);

            String htmlContent = templateEngine.process("order-confirmation-email", context);
            sendEmail(to, "Order Confirmed - " + orderNumber, htmlContent);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPaymentSuccessEmail(String to, String firstName, String orderNumber,
                                         String amount, String paymentReference) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("orderNumber", orderNumber);
            context.setVariable("amount", amount);
            context.setVariable("paymentReference", paymentReference);

            String htmlContent = templateEngine.process("payment-success-email", context);
            sendEmail(to, "Payment Successful - " + orderNumber, htmlContent);
            log.info("Payment success email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send payment success email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String firstName, String otp) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("expiryMinutes", 5);

            String htmlContent = templateEngine.process("password-reset-email", context);
            sendEmail(to, "Password Reset - E-Commerce App", htmlContent);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("noreply@ecommerce.com");
        mailSender.send(message);
    }
}
