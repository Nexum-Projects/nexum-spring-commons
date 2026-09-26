package com.nexum.commons.pagination;

import com.nexum.commons.response.DataResponse;
import com.nexum.commons.response.ListDTO;
import com.nexum.commons.response.PageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.domain.Specification;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PagingIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private TestItemRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        IntStream.rangeClosed(1, 25).forEach(i -> repository.save(new TestItem(String.format("item-%02d", i))));
    }

    @Test
    void firstPageHasLimitRowsAndMeta() {
        TestItemParams params = new TestItemParams();
        params.setLimit(10);

        DataResponse<String> result = find(params);

        PageDTO<String> page = (PageDTO<String>) result;
        assertThat(page.data()).hasSize(10).first().isEqualTo("item-01");
        assertThat(page.meta().totalObjects()).isEqualTo(25);
        assertThat(page.meta().totalPages()).isEqualTo(3);
        assertThat(page.meta().hasNextPage()).isTrue();
    }

    @Test
    void lastPageHasTheRemainder() {
        TestItemParams params = new TestItemParams();
        params.setLimit(10);
        params.setPage(3);

        assertThat(find(params).data()).containsExactly("item-21", "item-22", "item-23", "item-24", "item-25");
    }

    @Test
    void paginationFalseReturnsEverythingAsAList() {
        TestItemParams params = new TestItemParams();
        params.setPagination(false);

        DataResponse<String> result = find(params);

        assertThat(result).isInstanceOf(ListDTO.class);
        assertThat(result.data()).hasSize(25);
    }

    @Test
    void hugeLimitIsCapped() {
        TestItemParams params = new TestItemParams();
        params.setLimit(1_000_000);

        PageDTO<String> page = (PageDTO<String>) find(params);

        assertThat(page.meta().limit()).isEqualTo(BaseQueryParamsDTO.MAX_LIMIT);
    }

    @Test
    void descendingOrderIsApplied() {
        TestItemParams params = new TestItemParams();
        params.setOrder("desc");
        params.setLimit(2);

        assertThat(find(params).data()).containsExactly("item-25", "item-24");
    }

    private DataResponse<String> find(TestItemParams params) {
        return Paging.find(params, repository, Specification.unrestricted(), TestItem::getName);
    }
}
