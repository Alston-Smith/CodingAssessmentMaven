package com.alstonsmith.model;

import java.util.HashSet;
import java.util.Set;

public class Team {
    private String id;
    private String name;
    private String description;
    private Set<String> memberIds = new HashSet<>();

    public Team() {
    }

    public Team(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds;
    }
}
