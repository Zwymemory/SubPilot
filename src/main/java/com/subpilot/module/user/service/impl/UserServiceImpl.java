package com.subpilot.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.module.user.dto.UserUpdateRequest;
import com.subpilot.module.user.entity.UserEntity;
import com.subpilot.module.user.mapper.UserMapper;
import com.subpilot.module.user.service.UserService;
import com.subpilot.module.user.vo.UserVO;
import com.subpilot.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .last("LIMIT 1");
        return Optional.ofNullable(userMapper.selectOne(wrapper));
    }

    @Override
    public UserEntity getByIdOrThrow(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getCurrentUser() {
        return toVO(getByIdOrThrow(UserContext.getUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateCurrentUser(UserUpdateRequest request) {
        Long userId = UserContext.getUserId();
        UserEntity updateUser = new UserEntity();
        updateUser.setId(userId);
        updateUser.setNickname(request.nickname());
        updateUser.setAvatarUrl(request.avatarUrl());
        updateUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(updateUser);
        return toVO(getByIdOrThrow(userId));
    }

    @Override
    public UserVO toVO(UserEntity user) {
        return new UserVO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
