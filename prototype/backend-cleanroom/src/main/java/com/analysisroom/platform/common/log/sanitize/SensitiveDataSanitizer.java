package com.analysisroom.platform.common.log.sanitize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class SensitiveDataSanitizer {

    private static final String MASKED_KEY = "maskedField";
    private static final String MASKED_VALUE = "***";

    private final ObjectMapper objectMapper;

    public SensitiveDataSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toSanitizedJson(Object value) {
        try {
            return objectMapper.writeValueAsString(sanitize(value));
        } catch (RuntimeException ex) {
            return String.valueOf(sanitize(String.valueOf(value)));
        } catch (Exception ex) {
            return "\"<unserializable>\"";
        }
    }

    public Object sanitize(Object value) {
        return sanitizeValue("", value);
    }

    private Object sanitizeValue(String key, Object value) {
        if (isSensitiveKey(key)) {
            return MASKED_VALUE;
        }
        if (value == null) {
            return null;
        }
        if (isSkippedType(value)) {
            return "<skipped>";
        }
        if (isSimpleValue(value)) {
            return value;
        }
        if (value instanceof Map<?, ?> map) {
            return sanitizeMap(map);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                .map(item -> sanitizeValue("", item))
                .toList();
        }
        if (value.getClass().isArray()) {
            ArrayList<Object> list = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            for (int index = 0; index < length; index++) {
                list.add(sanitizeValue("", java.lang.reflect.Array.get(value, index)));
            }
            return list;
        }
        try {
            return sanitizeJsonNode(objectMapper.valueToTree(value));
        } catch (IllegalArgumentException ex) {
            return String.valueOf(value);
        }
    }

    private Map<String, Object> sanitizeMap(Map<?, ?> map) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String originalKey = String.valueOf(entry.getKey());
            boolean sensitive = isSensitiveKey(originalKey);
            String outputKey = sensitive ? nextMaskedKey(sanitized) : originalKey;
            sanitized.put(outputKey, sensitive ? MASKED_VALUE : sanitizeValue(originalKey, entry.getValue()));
        }
        return sanitized;
    }

    private JsonNode sanitizeJsonNode(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode child : node) {
                arrayNode.add(sanitizeJsonNode(child));
            }
            return arrayNode;
        }
        ObjectNode objectNode = objectMapper.createObjectNode();
        node.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            if (isSensitiveKey(fieldName)) {
                objectNode.put(nextMaskedKey(objectNode), MASKED_VALUE);
            } else {
                objectNode.set(fieldName, sanitizeJsonNode(entry.getValue()));
            }
        });
        return objectNode;
    }

    private String nextMaskedKey(Map<String, Object> map) {
        if (!map.containsKey(MASKED_KEY)) {
            return MASKED_KEY;
        }
        int index = 2;
        while (map.containsKey(MASKED_KEY + index)) {
            index++;
        }
        return MASKED_KEY + index;
    }

    private String nextMaskedKey(ObjectNode node) {
        if (!node.has(MASKED_KEY)) {
            return MASKED_KEY;
        }
        int index = 2;
        while (node.has(MASKED_KEY + index)) {
            index++;
        }
        return MASKED_KEY + index;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key == null
            ? ""
            : key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return normalized.contains("password")
            || normalized.contains("token")
            || normalized.contains("authorization")
            || normalized.contains("secret")
            || normalized.contains("credential")
            || normalized.contains("cookie")
            || normalized.contains("setcookie");
    }

    private boolean isSimpleValue(Object value) {
        return value instanceof String
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof Enum<?>;
    }

    private boolean isSkippedType(Object value) {
        return value instanceof ServletRequest
            || value instanceof ServletResponse
            || value instanceof BindingResult
            || value instanceof MultipartFile;
    }
}
