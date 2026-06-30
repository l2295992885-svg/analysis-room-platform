package com.analysisroom.platform.common.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void calculatesPagesFromTotalAndPageSize() {
        PageResult<String> result = PageResult.of(List.of("a", "b"), 21, 2, 10);

        assertThat(result.records()).containsExactly("a", "b");
        assertThat(result.total()).isEqualTo(21);
        assertThat(result.pageNo()).isEqualTo(2);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.pages()).isEqualTo(3);
    }
}
