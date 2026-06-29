package org.dromara.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * Sanitizes sensitive values before they are written to application logs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogSanitizer {

    private static final String REDACTED = "[REDACTED]";

    private static final Pattern JWT_PATTERN = Pattern.compile(
        "(?i)(bearer\\s+)?([A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+)"
    );

    private static final Pattern SENSITIVE_KEY_VALUE_PATTERN = Pattern.compile(
        "(?i)\\b(authorization|cookie|set-cookie|token|access[_-]?token|refresh[_-]?token|password|oldPassword|newPassword|secret|credential)\\b(\\s*[:=]\\s*)([^,;\\s}\\\"]+)"
    );

    public static String sanitize(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String sanitized = JWT_PATTERN.matcher(value).replaceAll("$1" + REDACTED);
        return SENSITIVE_KEY_VALUE_PATTERN.matcher(sanitized).replaceAll("$1$2" + REDACTED);
    }

}
