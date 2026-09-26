package com.nexum.commons.response;

import java.util.List;

/** Lista paginada: {@code { "data": [...], "meta": {...} }}. */
public record PageDTO<T>(List<T> data, PageMetaDTO meta) implements DataResponse<T> {
}
