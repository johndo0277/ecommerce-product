package com.johndo.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.johndo.product.dto.Message;
import com.johndo.product.messaging.RabbitMQProducer;

@RestController
public class RabbitMQTestController {
    private final RabbitMQProducer rabbitMQProducer;

    public RabbitMQTestController(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @GetMapping("/api/v1/rabbitmq/test")
    public ResponseEntity<String> testRabbitMQ(@RequestParam String message) {
        rabbitMQProducer.sendMessage(new Message(message));
        return ResponseEntity.ok("Message sent to RabbitMQ: " + message);
    }

}
