package com.analysisroom.platform.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.analysisroom.platform.common.trace.TraceIdFilter;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(TraceIdFilter.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsUnifiedResponseWithTraceId() throws Exception {
        mockMvc.perform(get("/api/health").header("X-Trace-Id", "test-trace-id"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Trace-Id", "test-trace-id"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.traceId").value("test-trace-id"))
            .andExpect(jsonPath("$.timestamp", not(blankOrNullString())))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.service").value("analysis-room-platform-backend"));
    }
}
