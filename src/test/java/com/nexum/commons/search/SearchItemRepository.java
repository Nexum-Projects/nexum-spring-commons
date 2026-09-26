package com.nexum.commons.search;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SearchItemRepository extends JpaRepository<SearchItem, Long>, JpaSpecificationExecutor<SearchItem> {
}
