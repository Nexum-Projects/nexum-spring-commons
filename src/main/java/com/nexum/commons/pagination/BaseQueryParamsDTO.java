package com.nexum.commons.pagination;

public abstract class BaseQueryParamsDTO {
    /** Tope de filas por página: evita que ?limit=1000000 traiga toda la tabla. */
    public static final int MAX_LIMIT = 100;

    private Integer page = 1;
    private Integer limit = 10;
    private String order = "ASC";
    private Boolean pagination = true;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null && page > 0 ? page : 1;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit != null && limit > 0 ? Math.min(limit, MAX_LIMIT) : 10;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        if (order != null && (order.equalsIgnoreCase("ASC") || order.equalsIgnoreCase("DESC"))) {
            this.order = order.toUpperCase();
        } else {
            this.order = "ASC";
        }
    }

    public Boolean getPagination() {
        return pagination;
    }

    public void setPagination(Boolean pagination) {
        this.pagination = pagination != null ? pagination : true;
    }
}
