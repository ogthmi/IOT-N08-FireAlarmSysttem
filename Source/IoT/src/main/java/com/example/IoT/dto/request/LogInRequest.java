package com.example.IoT.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LogInRequest {
    @NotEmpty(message = "Tên tài khoản không được để trống")
    private String username;
    @NotEmpty(message = "Mật khẩu không được để trống")
    private String password;
}
