package com.example.IoT.service;


import com.example.IoT.dto.ApiResponse;
import com.example.IoT.dto.request.LogInRequest;
import com.example.IoT.dto.request.UserRequest;
import com.example.IoT.dto.request.threshold.ThresholdDTO;
import com.example.IoT.dto.response.TokenResponse;
import com.example.IoT.dto.response.UserOutputV2;
import com.example.IoT.entity.UserEntity;
import com.example.IoT.exception.AppException;
import com.example.IoT.exception.ErrorCode;
import com.example.IoT.mapper.UserMapper;
import com.example.IoT.repository.UserRepository;
import com.example.IoT.security.TokenHelper;
import lombok.AllArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public TokenResponse signUp(UserRequest signUpRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        signUpRequest.setPassword(BCrypt.hashpw(signUpRequest.getPassword(), BCrypt.gensalt()));
        UserEntity userEntity = UserEntity.builder()
                .username(signUpRequest.getUsername())
                .password(signUpRequest.getPassword())
                .role(signUpRequest.getRole())
                .build();
        userRepository.save(userEntity);
        return TokenResponse.builder()
                .accessToken(TokenHelper.generateToken(userEntity))
                .build();
    }

    @Transactional
    public TokenResponse logIn(LogInRequest loginRequest) {
        UserEntity userEntity = userRepository.findByUsername(loginRequest.getUsername());
        if (Objects.isNull(userEntity)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (!BCrypt.checkpw(loginRequest.getPassword(), userEntity.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }
        return TokenResponse.builder()
                .accessToken(TokenHelper.generateToken(userEntity))
                .build();
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserOutputV2> getUserInformation(String accessToken){
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        return ApiResponse.<UserOutputV2>builder()
                .message("OK")
                .code(200)
                .result(userMapper.getOutputFromEntity(userEntity))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserOutputV2> getUserInformationPage(Pageable pageable){
        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        return userEntityPage.map(u -> userMapper.getOutputFromEntity(u));
    }
}
