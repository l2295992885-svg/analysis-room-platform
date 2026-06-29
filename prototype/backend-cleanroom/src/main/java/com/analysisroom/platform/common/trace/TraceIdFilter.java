package com.analysisroom.platform.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        TraceIdHolder.setTraceId(traceId);
        MDC.put(TraceConstants.MDC_TRACE_ID_KEY, traceId);
        response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceConstants.MDC_TRACE_ID_KEY);
            TraceIdHolder.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerValue = request.getHeader(TraceConstants.TRACE_ID_HEADER);
        if (StringUtils.hasText(headerValue) && headerValue.length() <= 128) {
            return headerValue.trim();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
