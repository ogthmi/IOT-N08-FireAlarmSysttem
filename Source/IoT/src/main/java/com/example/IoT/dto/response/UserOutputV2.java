package com.example.IoT.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOutputV2 {
    private Long id;
    private String username;
    private String phoneNumber;
    private String role;
}
