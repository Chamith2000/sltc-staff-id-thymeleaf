package com.oexil.staffid.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MediaType {
    IMAGE("image"),
    VIDEO("Update"),
    DOCUMENT("Assign");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MediaType fromString(String value) {
        for (MediaType type : MediaType.values()) {
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


