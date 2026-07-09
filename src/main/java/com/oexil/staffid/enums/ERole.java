package com.oexil.staffid.enums;

import lombok.Getter;

@Getter
public enum ERole {
    ROLE_ADMIN("admin"),
    ROLE_MANAGER("manager"),
    ROLE_USER("user");

    private final String value;

    ERole(String value) {
        this.value = value;
    }

}

