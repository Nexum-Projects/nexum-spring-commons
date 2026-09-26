package com.nexum.commons.pagination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseQueryParamsDTOTest {
    private static class Params extends BaseQueryParamsDTO {
    }

    @Test
    void defaultsAreFirstPageOfTenAscending() {
        Params params = new Params();
        assertEquals(1, params.getPage());
        assertEquals(10, params.getLimit());
        assertEquals("ASC", params.getOrder());
    }

    @Test
    void limitIsCappedSoATableCannotBeDumpedInOneRequest() {
        Params params = new Params();
        params.setLimit(1_000_000);
        assertEquals(BaseQueryParamsDTO.MAX_LIMIT, params.getLimit());
    }

    @Test
    void invalidValuesFallBackToDefaults() {
        Params params = new Params();
        params.setPage(-3);
        params.setLimit(0);
        params.setOrder("sideways");
        assertEquals(1, params.getPage());
        assertEquals(10, params.getLimit());
        assertEquals("ASC", params.getOrder());
    }
}
