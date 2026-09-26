package com.nexum.commons.search;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class SearchItem {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private boolean active;

    protected SearchItem() {
    }

    public SearchItem(String name, String email, boolean active) {
        this.name = name;
        this.email = email;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }
}
