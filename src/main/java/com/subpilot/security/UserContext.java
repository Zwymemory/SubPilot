package com.subpilot.security;

import com.subpilot.common.exception.BusinessException;
import com.subpilot.common.exception.ErrorCode;

public final class UserContext {

    private static final ThreadLocal<LoginUser> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        CURRENT_USER.set(loginUser);
    }

    public static LoginUser get() {
        LoginUser loginUser = CURRENT_USER.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long getUserId() {
        return get().userId();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
