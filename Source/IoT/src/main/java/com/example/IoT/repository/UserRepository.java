package com.example.IoT.repository;

import com.example.IoT.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);
    UserEntity findByUsername(String username);
    List<UserEntity> findAllByIdIn(Set<Long> userIds);
}
