package com.example.IoT.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")// endpoint client sẽ connect
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Cho phép server gửi message đến client qua /queue hoặc /topic
        registry.enableSimpleBroker("/data", "/notification");

        // Tất cả request từ client gửi lên phải bắt đầu bằng /app
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix dành cho message gửi theo user
        registry.setUserDestinationPrefix("/user");
    }
}
