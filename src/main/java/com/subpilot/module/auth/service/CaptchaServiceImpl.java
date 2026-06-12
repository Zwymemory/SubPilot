package com.subpilot.module.auth.service;

import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;
import com.subpilot.module.auth.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final String CAPTCHA_KEY_PREFIX = "subpilot:auth:captcha:";
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 5;
    private static final int IMAGE_WIDTH = 140;
    private static final int IMAGE_HEIGHT = 48;

    private final StringRedisTemplate stringRedisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public CaptchaVO generate() {
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        try {
            stringRedisTemplate.opsForValue().set(captchaKey(captchaId), code, CAPTCHA_TTL);
            return new CaptchaVO(captchaId, drawImage(code), CAPTCHA_TTL.toSeconds());
        } catch (RuntimeException exception) {
            log.warn("Generate captcha failed: captchaId={}", captchaId, exception);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码生成失败");
        }
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码不能为空");
        }
        String key = captchaKey(captchaId.trim());
        String expectedCode;
        try {
            expectedCode = stringRedisTemplate.opsForValue().get(key);
            stringRedisTemplate.delete(key);
        } catch (RuntimeException exception) {
            log.warn("Read captcha failed: captchaId={}", captchaId, exception);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码服务暂不可用");
        }
        if (!StringUtils.hasText(expectedCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码已过期");
        }
        if (!expectedCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "验证码错误");
        }
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            builder.append(CHAR_POOL.charAt(secureRandom.nextInt(CHAR_POOL.length())));
        }
        return builder.toString();
    }

    private String drawImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(248, 250, 252));
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            drawNoise(graphics);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
            for (int index = 0; index < code.length(); index++) {
                graphics.setColor(randomTextColor());
                int x = 16 + index * 23;
                int y = 32 + secureRandom.nextInt(7) - 3;
                double angle = (secureRandom.nextInt(31) - 15) * Math.PI / 180;
                graphics.rotate(angle, x, y);
                graphics.drawString(String.valueOf(code.charAt(index)), x, y);
                graphics.rotate(-angle, x, y);
            }
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码图片生成失败");
        }
    }

    private void drawNoise(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1.4F));
        for (int index = 0; index < 8; index++) {
            graphics.setColor(new Color(120 + secureRandom.nextInt(100), 120 + secureRandom.nextInt(100), 120 + secureRandom.nextInt(100)));
            graphics.drawLine(
                    secureRandom.nextInt(IMAGE_WIDTH),
                    secureRandom.nextInt(IMAGE_HEIGHT),
                    secureRandom.nextInt(IMAGE_WIDTH),
                    secureRandom.nextInt(IMAGE_HEIGHT)
            );
        }
        for (int index = 0; index < 35; index++) {
            graphics.setColor(new Color(120 + secureRandom.nextInt(100), 120 + secureRandom.nextInt(100), 120 + secureRandom.nextInt(100)));
            graphics.fillOval(secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT), 2, 2);
        }
    }

    private Color randomTextColor() {
        return new Color(30 + secureRandom.nextInt(80), 50 + secureRandom.nextInt(80), 80 + secureRandom.nextInt(80));
    }

    private String captchaKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId.toLowerCase(Locale.ROOT);
    }
}
