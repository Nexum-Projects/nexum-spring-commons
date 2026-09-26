package com.nexum.commons.pagination;

import java.util.Set;

public abstract class BaseSortableQueryParamsDTO extends BaseQueryParamsDTO {
    private String orderBy = defaultOrderBy();

    protected abstract String defaultOrderBy();

    protected abstract Set<String> allowedOrderByFields();

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        String normalizedOrderBy = orderBy == null || orderBy.isBlank() ? defaultOrderBy() : orderBy.trim();
        if (!allowedOrderByFields().contains(normalizedOrderBy)) {
            throw new IllegalArgumentException("Invalid orderBy field: " + normalizedOrderBy);
        }
        this.orderBy = normalizedOrderBy;
    }
}
