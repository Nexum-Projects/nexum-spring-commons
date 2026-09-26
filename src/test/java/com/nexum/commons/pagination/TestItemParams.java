package com.nexum.commons.pagination;

import java.util.Set;

class TestItemParams extends BaseSortableQueryParamsDTO {
    @Override
    protected String defaultOrderBy() {
        return "name";
    }

    @Override
    protected Set<String> allowedOrderByFields() {
        return Set.of("name");
    }
}
