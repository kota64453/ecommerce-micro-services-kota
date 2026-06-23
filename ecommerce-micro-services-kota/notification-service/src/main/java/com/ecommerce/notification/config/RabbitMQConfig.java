package com.ecommerce.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "notification.exchange";
    public static final String OTP_QUEUE = "otp.queue";
    public static final String WELCOME_QUEUE = "welcome.queue";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success.queue";
    public static final String FORGOT_PASSWORD_QUEUE = "forgot.password.queue";
    public static final String OTP_ROUTING_KEY = "otp.send";
    public static final String WELCOME_ROUTING_KEY = "welcome.send";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";
    public static final String FORGOT_PASSWORD_ROUTING_KEY = "password.reset";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue otpQueue() { return new Queue(OTP_QUEUE, true); }

    @Bean
    public Queue welcomeQueue() { return new Queue(WELCOME_QUEUE, true); }

    @Bean
    public Queue orderCreatedQueue() { return new Queue(ORDER_CREATED_QUEUE, true); }

    @Bean
    public Queue paymentSuccessQueue() { return new Queue(PAYMENT_SUCCESS_QUEUE, true); }

    @Bean
    public Queue forgotPasswordQueue() { return new Queue(FORGOT_PASSWORD_QUEUE, true); }

    @Bean
    public Binding otpBinding() {
        return BindingBuilder.bind(otpQueue()).to(notificationExchange()).with(OTP_ROUTING_KEY);
    }

    @Bean
    public Binding welcomeBinding() {
        return BindingBuilder.bind(welcomeQueue()).to(notificationExchange()).with(WELCOME_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(notificationExchange()).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentSuccessBinding() {
        return BindingBuilder.bind(paymentSuccessQueue()).to(notificationExchange()).with(PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding forgotPasswordBinding() {
        return BindingBuilder.bind(forgotPasswordQueue()).to(notificationExchange()).with(FORGOT_PASSWORD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
