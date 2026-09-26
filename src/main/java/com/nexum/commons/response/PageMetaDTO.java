package com.nexum.commons.response;

/** Metadatos de una página. {@code page} empieza en 1; un resultado vacío tiene una página. */
public record PageMetaDTO(int page, int limit, int totalObjects, int totalPages,
                          boolean hasPreviousPage, boolean hasNextPage) {

    public PageMetaDTO(int page, int limit, int totalObjects) {
        this(page, limit, totalObjects, totalPages(totalObjects, limit));
    }

    private PageMetaDTO(int page, int limit, int totalObjects, int totalPages) {
        this(page, limit, totalObjects, totalPages, page > 1, page < totalPages);
    }

    private static int totalPages(int totalObjects, int limit) {
        return totalObjects == 0 ? 1 : (totalObjects + limit - 1) / limit;
    }
}
