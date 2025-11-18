package com.example.IoT.mapper;

import com.example.IoT.dto.request.UserRequest;
import com.example.IoT.dto.response.UserOutputV2;
import com.example.IoT.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserOutputV2 getOutputFromEntity(UserEntity userEntity);
    UserEntity getEntityFromRequest(UserRequest signUpRequest);
}
