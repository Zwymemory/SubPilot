package com.subpilot.module.user.controller;

import com.subpilot.common.response.ApiResponse;
import com.subpilot.module.user.dto.UserUpdateRequest;
import com.subpilot.module.user.service.UserService;
import com.subpilot.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user")
    @GetMapping("/me")
    public ApiResponse<UserVO> me() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ApiResponse<UserVO> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success(userService.updateCurrentUser(request));
    }
}
