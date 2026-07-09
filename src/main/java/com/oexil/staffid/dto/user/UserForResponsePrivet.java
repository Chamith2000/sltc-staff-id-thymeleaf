package com.oexil.staffid.dto.user;

import com.oexil.staffid.model.masters.Role;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Data
public class UserForResponsePrivet {
    @Setter(AccessLevel.NONE)
    String firstName;
    @Setter(AccessLevel.NONE)
    String lastName;
    String email, phoneNumber, status, createDate, countryCode, countryCodePrefix, fcmToken, role, fullName;
    Long id;
    boolean profileCreated, profileLocked;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    Set<Role> roles;

    public void setRoles(Set<Role> roles) {
        this.role = Objects.requireNonNull(roles.stream().findFirst().orElse(null)).getName().getValue();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateName();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateName();
    }

    private void updateName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        } else if (firstName != null) {
            this.fullName = firstName; // Set name to firstName if lastName is null
        } else if (lastName != null) {
            this.fullName = lastName; // Set name to lastName if firstName is null
        } else {
            this.fullName = null; // Set name to null if both firstName and lastName are null
        }
    }
}
