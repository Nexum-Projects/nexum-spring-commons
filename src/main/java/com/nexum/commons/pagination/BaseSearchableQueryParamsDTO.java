package com.nexum.commons.pagination;


import java.util.Set;

public abstract class BaseSearchableQueryParamsDTO extends BaseSortableQueryParamsDTO {
    private String query;

    protected abstract Set<String> searchableFields();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null || query.isBlank() ? null : query.trim();
    }

    public Set<String> getSearchableFields() {
        return searchableFields();
    }
}
