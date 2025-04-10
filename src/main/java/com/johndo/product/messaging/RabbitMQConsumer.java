package com.johndo.product.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.johndo.product.dto.Message;

@Service
public class RabbitMQConsumer {

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void receiveMessage(Message message) {
        System.out.println("Message received: " + message.getContent());
        // Add your business logic here to process the message
    }

}