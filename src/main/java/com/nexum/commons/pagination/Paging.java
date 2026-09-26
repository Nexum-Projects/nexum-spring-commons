package com.nexum.commons.pagination;

import com.nexum.commons.response.DataResponse;
import com.nexum.commons.response.ListDTO;
import com.nexum.commons.response.PageDTO;
import com.nexum.commons.response.PageMetaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.function.Function;

/** Arma un listado a partir de los parámetros de consulta. */
public final class Paging {
    private Paging() {
    }

    /**
     * Devuelve {@link ListDTO} si {@code pagination=false} y {@link PageDTO} en otro caso, ordenado por
     * {@code orderBy} y {@code order}.
     * <p>
     * El {@code mapper} recibe entidades: llamar desde un service {@code @Transactional(readOnly = true)} para que
     * pueda leer relaciones lazy, y cargar con {@code @EntityGraph} las que el DTO necesite (evita N+1).
     */
    public static <E, R> DataResponse<R> find(
            BaseSortableQueryParamsDTO params,
            JpaSpecificationExecutor<E> repository,
            Specification<E> specification,
            Function<? super E, ? extends R> mapper
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(params.getOrder()), params.getOrderBy());

        if (Boolean.FALSE.equals(params.getPagination())) {
            return new ListDTO<>(repository.findAll(specification, sort).stream().<R>map(mapper).toList());
        }

        Page<E> page = repository.findAll(specification, PageRequest.of(params.getPage() - 1, params.getLimit(), sort));
        int total = (int) Math.min(page.getTotalElements(), Integer.MAX_VALUE);
        return new PageDTO<>(page.getContent().stream().<R>map(mapper).toList(),
                new PageMetaDTO(params.getPage(), params.getLimit(), total));
    }
}
