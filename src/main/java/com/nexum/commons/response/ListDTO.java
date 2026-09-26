package com.nexum.commons.response;

import java.util.List;

/** Lista sin paginar ({@code pagination=false}): {@code { "data": [...] }}. */
public record ListDTO<T>(List<T> data) implements DataResponse<T> {
}
