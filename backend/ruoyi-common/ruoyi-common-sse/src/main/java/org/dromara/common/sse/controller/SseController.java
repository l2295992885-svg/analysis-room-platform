package org.dromara.common.sse.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.SseException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.sse.core.SseEmitterManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.UUID;

/**
 * SSE 控制器
 *
 * @author Lion Li
 */
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    private static final String SSE_TICKET_PREFIX = "security:sse-ticket:";
    private static final Duration SSE_TICKET_TTL = Duration.ofSeconds(30);

    private final SseEmitterManager sseEmitterManager;

    /**
     * 签发短期 SSE 连接票据。
     * <p>
     * EventSource 原生不支持设置 Authorization header，前端必须先用普通
     * Ajax 请求携带 Authorization header 换取一次性短票据，避免长期 token
     * 出现在 URL、浏览器历史和访问日志中。
     */
    @GetMapping(value = "${sse.path}/ticket")
    public R<String> ticket() {
        String tokenValue = StpUtil.getTokenValue();
        if (StringUtils.isBlank(tokenValue)) {
            throw new SseException("SSE ticket requires login");
        }
        String ticket = UUID.randomUUID().toString().replace("-", "");
        RedisUtils.setCacheObject(SSE_TICKET_PREFIX + ticket, tokenValue, SSE_TICKET_TTL);
        return R.ok(ticket);
    }

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(value = "sseTicket", required = false) String sseTicket) {
        if (StringUtils.isBlank(sseTicket)) {
            return null;
        }
        String ticketKey = SSE_TICKET_PREFIX + sseTicket;
        String tokenValue = RedisUtils.getCacheObject(ticketKey);
        RedisUtils.deleteObject(ticketKey);
        if (StringUtils.isBlank(tokenValue)) {
            return null;
        }
        LoginUser loginUser = LoginHelper.getLoginUser(tokenValue);
        if (loginUser == null) {
            return null;
        }
        Long userId = loginUser.getUserId();
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * 关闭 SSE 连接
     */
    @SaIgnore
    @GetMapping(value = "${sse.path}/close")
    public R<Void> close() {
        String tokenValue = StpUtil.getTokenValue();
        Long userId = LoginHelper.getUserId();
        sseEmitterManager.disconnect(userId, tokenValue);
        return R.ok();
    }

    // 以下为demo仅供参考 禁止使用 请在业务逻辑中使用工具发送而不是用接口发送
//    /**
//     * 向特定用户发送消息
//     *
//     * @param userId 目标用户的 ID
//     * @param msg    要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/send")
//    public R<Void> send(Long userId, String msg) {
//        SseMessageDto dto = new SseMessageDto();
//        dto.setUserIds(List.of(userId));
//        dto.setMessage(msg);
//        sseEmitterManager.publishMessage(dto);
//        return R.ok();
//    }
//
//    /**
//     * 向所有用户发送消息
//     *
//     * @param msg 要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/sendAll")
//    public R<Void> send(String msg) {
//        sseEmitterManager.publishAll(msg);
//        return R.ok();
//    }

    /**
     * 清理资源。此方法目前不执行任何操作，但避免因未实现而导致错误
     */
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }

}
