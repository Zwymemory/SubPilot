package com.subpilot.security;

import java.io.Serial;
import java.io.Serializable;

public record LoginUser(
        Long userId,
        String email,
        String nickname
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
