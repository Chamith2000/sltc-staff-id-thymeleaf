package com.oexil.staffid.model;

import com.oexil.staffid.enums.AuthProvider;
import com.oexil.staffid.enums.UserType;
import com.oexil.staffid.model.masters.Role;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class    User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "active")
    private boolean active;

    @Column(name = "approve")
    private Boolean approve;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_has_roles",
            joinColumns = @JoinColumn(name = "fk_idUser"),
            inverseJoinColumns = @JoinColumn(name = "fk_idRole"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    // Security
    @Column(name = "password_reset_code")
    private String passwordResetCode;

    @Column(name = "two_factor_auth")
    private boolean twoFactorAuth;

    @Column(name = "two_factor_auth_code")
    private String twoFactorAuthCode;

    @Column(name = "email_verification")
    private boolean emailVerification;

    @Column(name = "email_verification_code")
    private String emailVerificationCode;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
