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
    private String email;
    private String phone;
    private String role;
}
