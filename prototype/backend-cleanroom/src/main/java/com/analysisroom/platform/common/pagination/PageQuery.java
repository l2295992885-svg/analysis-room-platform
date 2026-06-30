package com.analysisroom.platform.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {

    @Min(value = 1, message = "pageNo must be greater than or equal to 1")
    private int pageNo = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    @Max(value = 200, message = "pageSize must be less than or equal to 200")
    private int pageSize = 20;

    private String sortBy;

    private String sortOrder = "desc";

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int offset() {
        return (pageNo - 1) * pageSize;
    }

    public boolean ascending() {
        return "asc".equalsIgnoreCase(sortOrder);
    }
}
