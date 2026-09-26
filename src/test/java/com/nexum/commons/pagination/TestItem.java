package com.nexum.commons.pagination;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class TestItem {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    protected TestItem() {
    }

    public TestItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
