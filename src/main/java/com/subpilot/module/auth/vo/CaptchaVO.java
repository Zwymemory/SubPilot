package com.subpilot.module.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Captcha response")
public record CaptchaVO(
        @Schema(description = "Captcha ID")
        String captchaId,

        @Schema(description = "Base64 PNG image data URL")
        String imageBase64,

        @Schema(description = "Expiration seconds")
        long expireSeconds
) {
}
