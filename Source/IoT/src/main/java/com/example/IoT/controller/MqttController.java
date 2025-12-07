package com.example.IoT.controller;

import com.example.IoT.config.MqttGateway;
import com.example.IoT.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mqtt")
@AllArgsConstructor
@CrossOrigin("*")
public class MqttController {

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gửi một tin nhắn đến topic mặc định.
     * Cách dùng: GET http://localhost:8080/api/mqtt/send?message=Hello
     */
    @GetMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
        try {
            // Sử dụng gateway để gửi tin nhắn
            mqttGateway.send(message);
            return ResponseEntity.ok("Message '" + message + "' sent to default topic!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Gửi một tin nhắn đến topic tùy chọn.
     * Cách dùng: GET http://localhost:8080/api/mqtt/send-topic?topic=my/custom/topic&message=Test
     */
    @PostMapping("/send-topic")
    public ApiResponse<?> sendMessageToTopic(
            @RequestParam("topic") String topic,
            @RequestBody Map<String, Object> payload) throws JsonProcessingException {
        String jsonPayload = objectMapper.writeValueAsString(payload);

        // Sử dụng hàm sendToTopic của gateway
        mqttGateway.sendToTopic(topic, jsonPayload);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("topic", topic);
        responseData.put("payload", payload);

        System.out.println("====================================================");
        System.out.println("CALL");

        return ApiResponse.builder()
                .code(200)
                .message("Gửi message thành công đến MQTT broker!")
                .result(responseData)
                .build();
    }
}