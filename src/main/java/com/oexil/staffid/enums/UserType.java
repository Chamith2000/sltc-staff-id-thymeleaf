package com.oexil.staffid.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
    SYSTEM("system"),
    USER("user");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UserType fromString(String value) {
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

