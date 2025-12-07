package com.example.IoT.controller;


import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.LogInRequest;
import com.example.IoT.dto.request.UserRequest;
import com.example.IoT.dto.request.user.InformationRequest;
import com.example.IoT.dto.response.TokenResponse;
import com.example.IoT.dto.response.UserOutputV2;
import com.example.IoT.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Đăng ký tài khoản")
    @PostMapping("sign-up")
    public ApiResponse<TokenResponse> signUp(@RequestBody @Valid UserRequest signUpRequest){
        return ApiResponse.<TokenResponse>builder()
                .result(userService.signUp(signUpRequest))
                .code(200)
                .message("Đăng ký thành công")
                .build();
    }

    @PostMapping("log-in")
    public ApiResponse<TokenResponse> logIn(@RequestBody @Valid LogInRequest logInRequest) {
        return ApiResponse.<TokenResponse>builder()
                .result(userService.logIn(logInRequest))
                .code(200)
                .message("Đăng nhập thành công")
                .build();
    }

    @Operation(summary = "Lấy thông tin cá nhân")
    @GetMapping
    public ApiResponse<UserOutputV2> getUserInformation(@RequestHeader("Authorization") String accessToken){
        return userService.getUserInformation(accessToken);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserOutputV2>> getAllUserInformation(Pageable  pageable){
        return ApiResponse.<Page<UserOutputV2>>builder()
                .code(200)
                .message("Lấy danh sách người dùng thành công")
                .result(userService.getUserInformationPage(pageable))
                .build();
    }

    @Operation(summary = "Thay đổi thông tin người dùng")
    @PutMapping
    public ApiResponse<?> changeInformation(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody InformationRequest informationRequest) {
        userService.changeInformation(accessToken, informationRequest);
        return ApiResponse.builder()
                .message("Thay đổi thông tin thành công")
                .code(200)
                .build();
    }

    @Operation(summary = "Xóa người dùng")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.builder()
                .code(200)
                .message("Xóa người dùng thành công")
                .build();
    }
}
