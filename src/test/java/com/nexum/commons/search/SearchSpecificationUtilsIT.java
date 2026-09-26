package com.nexum.commons.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SearchSpecificationUtilsIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine").withInitScript("unaccent.sql");

    @Autowired
    private SearchItemRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new SearchItem("José Pérez", "jose@example.com", true));
        repository.save(new SearchItem("Ana Gómez", "ana_g@example.com", true));
        repository.save(new SearchItem("Josefina Ruiz", "fina@example.com", false));
    }

    @Test
    void matchesIgnoringAccentsAndCaseOnlyAmongActive() {
        assertThat(search("JOSE")).containsExactly("José Pérez");
    }

    @Test
    void searchesAcrossEveryField() {
        assertThat(search("ana_g@")).containsExactly("Ana Gómez");
    }

    @Test
    void blankQueryOnlyFiltersByState() {
        assertThat(search("  ")).containsExactlyInAnyOrder("José Pérez", "Ana Gómez");
    }

    @Test
    void likeWildcardsInTheQueryAreLiteral() {
        // Sin escapar, "_" y "%" coinciden con cualquier carácter y devolverían todas las filas.
        assertThat(search("_")).containsExactly("Ana Gómez");
        assertThat(search("%")).isEmpty();
    }

    private List<String> search(String query) {
        return repository.findAll(SearchSpecificationUtils.activeAndTextQuery("active", true, query,
                Set.of("name", "email"))).stream().map(SearchItem::getName).toList();
    }
}
