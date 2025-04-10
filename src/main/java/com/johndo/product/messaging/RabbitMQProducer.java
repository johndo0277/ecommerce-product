package com.johndo.product.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.johndo.product.dto.Message;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange}")
    private String rabbitExchange;

    @Value("${spring.rabbitmq.routing-key}")
    private String rabbitRoutingKey;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Message message) {
        rabbitTemplate.convertAndSend(rabbitExchange, rabbitRoutingKey, message);
        System.out.println("Message sent: " + message.getContent());
    }
}