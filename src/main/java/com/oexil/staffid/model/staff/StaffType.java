package com.oexil.staffid.model.staff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "staff_type")
public class StaffType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @Column(name = "description")
    private String description;
}
