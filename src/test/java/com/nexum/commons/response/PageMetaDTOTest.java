package com.nexum.commons.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageMetaDTOTest {

    @Test
    void emptyResultStillHasOnePage() {
        PageMetaDTO meta = new PageMetaDTO(1, 10, 0);
        assertEquals(1, meta.totalPages());
        assertFalse(meta.hasNextPage());
        assertFalse(meta.hasPreviousPage());
    }

    @Test
    void computesPagesAndNavigationFlags() {
        PageMetaDTO first = new PageMetaDTO(1, 10, 25);
        assertEquals(3, first.totalPages());
        assertTrue(first.hasNextPage());
        assertFalse(first.hasPreviousPage());

        PageMetaDTO last = new PageMetaDTO(3, 10, 25);
        assertFalse(last.hasNextPage());
        assertTrue(last.hasPreviousPage());
    }

    @Test
    void pageAndListExposeTheSameDataThroughDataResponse() {
        DataResponse<String> page = new PageDTO<>(java.util.List.of("a"), new PageMetaDTO(1, 10, 1));
        DataResponse<String> list = new ListDTO<>(java.util.List.of("a"));
        assertEquals(page.data(), list.data());
    }
}
