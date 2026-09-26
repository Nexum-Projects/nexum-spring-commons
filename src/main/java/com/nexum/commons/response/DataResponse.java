package com.nexum.commons.response;

import java.util.List;

/** Respuesta de un listado: {@link PageDTO} (paginado) o {@link ListDTO} (sin paginar). */
public interface DataResponse<T> {
    List<T> data();
}
