package com.analysisroom.platform.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private long tokenExpiresIn = 86400;

    public long getTokenExpiresIn() {
        return tokenExpiresIn;
    }

    public void setTokenExpiresIn(long tokenExpiresIn) {
        this.tokenExpiresIn = tokenExpiresIn;
    }
}
