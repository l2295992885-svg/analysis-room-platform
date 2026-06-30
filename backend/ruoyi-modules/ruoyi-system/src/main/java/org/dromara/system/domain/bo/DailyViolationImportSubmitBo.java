package org.dromara.system.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class DailyViolationImportSubmitBo {

    private List<Long> rowIds;
}
