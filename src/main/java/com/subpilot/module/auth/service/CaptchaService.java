package com.subpilot.module.auth.service;

import com.subpilot.module.auth.vo.CaptchaVO;

public interface CaptchaService {

    CaptchaVO generate();

    void validate(String captchaId, String captchaCode);
}
