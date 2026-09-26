package com.nexum.commons.response;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El JSON es un contrato con los clientes: nombres y valores de los campos no cambian sin versión mayor.
 * Se compara por contenido; el orden de las claves de un objeto JSON no forma parte del contrato.
 */
class ResponseJsonTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void pageKeepsItsFieldNames() {
        String body = json.writeValueAsString(new PageDTO<>(List.of("a"), new PageMetaDTO(1, 10, 25)));
        assertEquals(json.readTree("{\"data\":[\"a\"],\"meta\":{\"page\":1,\"limit\":10,\"totalObjects\":25,"
                + "\"hasNextPage\":true,\"hasPreviousPage\":false,\"totalPages\":3}}"), json.readTree(body));
    }

    @Test
    void listAndDetailWrapInData() {
        assertEquals("{\"data\":[\"a\"]}", json.writeValueAsString(new ListDTO<>(List.of("a"))));
        assertEquals("{\"data\":\"a\"}", json.writeValueAsString(new PageDetailDTO<>("a")));
    }
}
