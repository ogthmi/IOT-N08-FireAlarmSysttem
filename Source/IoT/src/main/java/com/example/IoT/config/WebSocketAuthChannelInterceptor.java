package com.example.IoT.config;

import com.example.IoT.security.TokenHelper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        System.out.println("=======================CONNECT=============================");

        if (accessor != null) {
            if (accessor.getCommand() != null) {
                System.out.println("STOMP command: " + accessor.getCommand());
                System.out.println("Headers: " + accessor.toNativeHeaderMap());
            }

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                if (token == null) token = accessor.getFirstNativeHeader("authorization");

                if (token == null || token.isBlank()) {
                    System.out.println("Missing token! Connection will fail.");
                    throw new IllegalArgumentException("Missing Authorization header");
                }

                Long userId;
                try {
                    userId = TokenHelper.getUserIdFromToken(token);
                } catch (Exception e) {
                    System.out.println("Invalid token! " + e.getMessage());
                    throw new IllegalArgumentException("Invalid token");
                }

                accessor.setUser(new StompUser(String.valueOf(userId)));
                System.out.println("User set: " + userId);
            }
        }

        return message;
    }
}

