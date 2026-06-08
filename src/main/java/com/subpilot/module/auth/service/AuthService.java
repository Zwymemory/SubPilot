package com.subpilot.module.auth.service;

import com.subpilot.module.auth.dto.LoginRequest;
import com.subpilot.module.auth.dto.RegisterRequest;
import com.subpilot.module.auth.vo.LoginVO;

public interface AuthService {

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);
}
