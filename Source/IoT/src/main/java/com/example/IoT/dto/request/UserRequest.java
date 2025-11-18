package com.example.IoT.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotEmpty(message = "username không được để trống")
    private String username;
    @NotEmpty(message = "mật khẩu không được để trống")
    private String password;
    @NotEmpty
    private String role;
}
