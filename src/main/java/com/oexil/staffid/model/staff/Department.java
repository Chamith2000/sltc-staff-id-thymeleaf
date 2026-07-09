package com.oexil.staffid.model.staff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "department")
public class Department implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "staff_type")
    private StaffType staffType;

    @ManyToOne
    @JoinColumn(name = "faculty")
    private Faculty faculty;
}
