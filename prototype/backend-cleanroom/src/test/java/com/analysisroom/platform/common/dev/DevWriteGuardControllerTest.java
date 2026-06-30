package com.analysisroom.platform.common.dev;

import com.analysisroom.platform.auth.config.AuthWebConfiguration;
import com.analysisroom.platform.auth.model.AuthUser;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.exception.ForbiddenException;
import com.analysisroom.platform.common.exception.UnauthorizedException;
import com.analysisroom.platform.common.log.aspect.OperationLogAspect;
import com.analysisroom.platform.common.log.model.OperationLogRecord;
import com.analysisroom.platform.common.log.sanitize.SensitiveDataSanitizer;
import com.analysisroom.platform.common.log.service.OperationLogService;
import com.analysisroom.platform.common.security.CurrentUserProvider;
import com.analysisroom.platform.common.security.PermissionService;
import com.analysisroom.platform.common.security.aspect.RequirePermissionAspect;
import com.analysisroom.platform.common.trace.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@WebMvcTest(
    controllers = DevWriteGuardController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthWebConfiguration.class)
)
@Import({
    RequirePermissionAspect.class,
    OperationLogAspect.class,
    SensitiveDataSanitizer.class,
    TraceIdFilter.class
})
class DevWriteGuardControllerTest {

    private static final String SENSITIVE_BODY = """
        {
          "username": "tester",
          "password": "plain-password",
          "token": "plain-token",
          "Authorization": "Bearer plain-token",
          "nested": {
            "newPassword": "new-plain-password",
            "credential": "plain-credential"
          }
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private OperationLogService operationLogService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private AuthRepository authRepository;

    @Test
    void protectedActionReturns401WhenNotLoggedIn() throws Exception {
        doThrow(new UnauthorizedException("未登录或登录已过期"))
            .when(permissionService).requirePermission("system:user:add", false);

        mockMvc.perform(post("/api/dev/write-guard/success")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void protectedActionReturns403WhenPermissionMissing() throws Exception {
        doThrow(new ForbiddenException("无权执行该操作"))
            .when(permissionService).requirePermission("system:user:add", false);

        mockMvc.perform(post("/api/dev/write-guard/success")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void protectedActionAllowsAuthorizedUserAndWritesSuccessLog() throws Exception {
        mockCurrentUser();
        doNothing().when(permissionService).requirePermission("system:user:add", false);

        mockMvc.perform(post("/api/dev/write-guard/success")
                .contentType("application/json")
                .header("Authorization", "Bearer plain-token")
                .content(SENSITIVE_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        OperationLogRecord record = capturedOperationLog();
        assertThat(record.operationStatus()).isEqualTo("SUCCESS");
        assertThat(record.module()).isEqualTo("SYSTEM");
        assertThat(record.action()).isEqualTo("CREATE");
        assertSensitiveDataMasked(record.requestParams());
        assertSensitiveDataMasked(record.responseData());
    }

    @Test
    void failedActionWritesFailedLog() throws Exception {
        mockCurrentUser();
        doNothing().when(permissionService).requirePermission("system:user:add", false);

        mockMvc.perform(post("/api/dev/write-guard/fail")
                .contentType("application/json")
                .header("Authorization", "Bearer plain-token")
                .content(SENSITIVE_BODY))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.code").value(422));

        OperationLogRecord record = capturedOperationLog();
        assertThat(record.operationStatus()).isEqualTo("FAILED");
        assertThat(record.errorMessage()).contains("dev write guard failure");
        assertSensitiveDataMasked(record.requestParams());
    }

    private void mockCurrentUser() {
        when(currentUserProvider.currentUserIdOrNull()).thenReturn(1L);
        when(authRepository.findUserById(1L)).thenReturn(Optional.of(
            new AuthUser(1L, 1L, "综合分析室", "admin", "系统管理员", "hash", "ACTIVE")
        ));
    }

    private OperationLogRecord capturedOperationLog() {
        ArgumentCaptor<OperationLogRecord> captor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(operationLogService).write(captor.capture());
        return captor.getValue();
    }

    private void assertSensitiveDataMasked(String text) {
        assertThat(text).doesNotContain(
            "plain-password",
            "new-plain-password",
            "plain-token",
            "plain-credential",
            "password",
            "token",
            "Authorization",
            "credential"
        );
        assertThat(text).contains("maskedField");
    }
}
