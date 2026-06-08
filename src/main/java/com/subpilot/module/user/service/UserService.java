package com.subpilot.module.user.service;

import com.subpilot.module.user.dto.UserUpdateRequest;
import com.subpilot.module.user.entity.UserEntity;
import com.subpilot.module.user.vo.UserVO;

import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findByEmail(String email);

    UserEntity getByIdOrThrow(Long userId);

    UserVO getCurrentUser();

    UserVO updateCurrentUser(UserUpdateRequest request);

    UserVO toVO(UserEntity user);
}
