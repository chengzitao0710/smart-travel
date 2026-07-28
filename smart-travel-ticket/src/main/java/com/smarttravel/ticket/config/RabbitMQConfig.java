package com.smarttravel.ticket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public CustomExchange orderDelayExchange() {

        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                "order.delay.exchange",
                "x-delayed-message",
                true,
                false,
                args);
    }

    @Bean
    public Queue orderQueue() {

        return QueueBuilder
                .durable("order.queue")
                .build();
    }

    @Bean
    public Binding orderBinding() {

        return BindingBuilder.bind(orderQueue())
                .to(orderDelayExchange())
                .with("order.create")
                .noargs();
    }

    @Bean
    public DirectExchange paymentExchange() {

        return new DirectExchange(
                "payment.exchange",
                true,
                false);
    }

    @Bean
    public Queue paymentQueue() {

        return QueueBuilder
                .durable("payment.queue")
                .build();
    }

    @Bean
    public Binding paymentBinding() {

        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange())
                .with("payment.create");
    }

    @Bean
    public DirectExchange orderCancelExchange() {

        return new DirectExchange(
                "order.cancel.exchange",
                true,
                false);
    }

    @Bean
    public Queue orderCancelQueue() {

        return QueueBuilder
                .durable("order.cancel.queue")
                .build();
    }

    @Bean
    public Binding orderCancelBinding() {

        return BindingBuilder.bind(orderCancelQueue())
                .to(orderCancelExchange())
                .with("order.cancel");
    }
}