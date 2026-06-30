package org.dromara.common.websocket.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/**
 * WebSocket short ticket controller.
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(value = "websocket.enabled", havingValue = "true")
public class WebSocketTicketController {

    public static final String WS_TICKET_PREFIX = "security:ws-ticket:";
    private static final Duration WS_TICKET_TTL = Duration.ofSeconds(30);

    @GetMapping("${websocket.path}/ticket")
    public R<String> ticket() {
        String tokenValue = StpUtil.getTokenValue();
        if (StringUtils.isBlank(tokenValue)) {
            return R.fail("WebSocket ticket requires login");
        }
        String ticket = UUID.randomUUID().toString().replace("-", "");
        RedisUtils.setCacheObject(WS_TICKET_PREFIX + ticket, tokenValue, WS_TICKET_TTL);
        return R.ok(ticket);
    }
}
