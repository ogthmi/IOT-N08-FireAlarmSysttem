package com.example.IoT.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    // Gửi tin nhắn với topic mặc định (đã cấu hình trong MessageHandler)
    void send(String payload);

    // Gửi tin nhắn với topic tùy chọn
    void sendToTopic(@Header(MqttHeaders.TOPIC) String topic, String payload);
}