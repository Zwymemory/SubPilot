package com.subpilot.module.auth.vo;

import com.subpilot.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response")
public record LoginVO(
        @Schema(description = "JWT access token")
        String accessToken,
        UserVO user
) {
}
