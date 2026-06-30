package com.analysisroom.platform.common.security;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long requireLoginUserId() {
        StpUtil.checkLogin();
        return StpUtil.getLoginIdAsLong();
    }

    public Long currentUserIdOrNull() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
