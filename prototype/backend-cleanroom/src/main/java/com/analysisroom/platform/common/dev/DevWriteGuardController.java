package com.analysisroom.platform.common.dev;

import com.analysisroom.platform.common.api.ApiResponse;
import com.analysisroom.platform.common.exception.BusinessException;
import com.analysisroom.platform.common.log.annotation.OperationLog;
import com.analysisroom.platform.common.security.annotation.RequirePermission;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/dev/write-guard")
public class DevWriteGuardController {

    @PostMapping("/success")
    @RequirePermission("system:user:add")
    @OperationLog(
        module = "SYSTEM",
        action = "CREATE",
        description = "dev profile write guard success verification",
        recordRequest = true,
        recordResponse = true
    )
    public ApiResponse<Map<String, Object>> success(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(Map.of(
            "accepted", true,
            "payload", payload
        ));
    }

    @PostMapping("/fail")
    @RequirePermission("system:user:add")
    @OperationLog(
        module = "SYSTEM",
        action = "UPDATE",
        description = "dev profile write guard failure verification",
        recordRequest = true,
        recordResponse = true
    )
    public ApiResponse<Map<String, Object>> fail(@RequestBody Map<String, Object> payload) {
        throw new BusinessException("dev write guard failure");
    }
}
