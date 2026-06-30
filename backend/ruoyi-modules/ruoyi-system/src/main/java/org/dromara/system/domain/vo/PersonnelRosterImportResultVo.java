package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PersonnelRosterImportResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sheetName;

    private Integer headerRowIndex = 0;

    private Integer totalRows = 0;

    private Integer insertedRows = 0;

    private Integer updatedRows = 0;

    private Integer failedRows = 0;

    private List<String> errors = new ArrayList<>();
}
