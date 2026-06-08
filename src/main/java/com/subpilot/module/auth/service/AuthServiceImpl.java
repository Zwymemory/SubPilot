package com.subpilot.module.auth.service;

import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.module.auth.dto.LoginRequest;
import com.subpilot.module.auth.dto.RegisterRequest;
import com.subpilot.module.auth.vo.LoginVO;
import com.subpilot.module.category.service.CategoryService;
import com.subpilot.module.user.entity.UserEntity;
import com.subpilot.module.user.enums.UserStatus;
import com.subpilot.module.user.mapper.UserMapper;
import com.subpilot.module.user.service.UserService;
import com.subpilot.module.user.vo.UserVO;
import com.subpilot.security.JwtTokenProvider;
import com.subpilot.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserService userService;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        userService.findByEmail(email).ifPresent(user -> {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已注册");
        });

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname().trim());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeleted(0);
        userMapper.insert(user);

        categoryService.initializeDefaultCategories(user.getId());
        log.info("Registered new user: userId={}, email={}", user.getId(), user.getEmail());

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误"));
        if (!UserStatus.ACTIVE.name().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被禁用");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }
        log.info("User logged in: userId={}, email={}", user.getId(), user.getEmail());
        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(UserEntity user) {
        UserVO userVO = userService.toVO(user);
        String token = jwtTokenProvider.generateToken(new LoginUser(user.getId(), user.getEmail(), user.getNickname()));
        return new LoginVO(token, userVO);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
