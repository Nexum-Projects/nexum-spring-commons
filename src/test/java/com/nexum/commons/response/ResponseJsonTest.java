package com.nexum.commons.response;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** El JSON es un contrato con los clientes: estos nombres de campo no cambian sin versión mayor. */
class ResponseJsonTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void pageKeepsItsFieldNames() {
        String body = json.writeValueAsString(new PageDTO<>(List.of("a"), new PageMetaDTO(1, 10, 25)));
        assertEquals("{\"data\":[\"a\"],\"meta\":{\"page\":1,\"limit\":10,\"totalObjects\":25,\"totalPages\":3,"
                + "\"hasPreviousPage\":false,\"hasNextPage\":true}}", body);
    }

    @Test
    void listAndDetailWrapInData() {
        assertEquals("{\"data\":[\"a\"]}", json.writeValueAsString(new ListDTO<>(List.of("a"))));
        assertEquals("{\"data\":\"a\"}", json.writeValueAsString(new PageDetailDTO<>("a")));
    }
}
