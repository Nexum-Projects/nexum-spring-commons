package com.nexum.commons.pagination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestItemRepository extends JpaRepository<TestItem, Long>, JpaSpecificationExecutor<TestItem> {
}
