package com.oexil.staffid.model.staff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;


    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "nic")
    private String nic;

    @Column(name = "emp_no")
    private String empNo;

    @Column(name = "designation")
    private String designation;

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "issue_date")
    private Date issueDate;

    @Column(name = "printed")
    private Boolean printed;

    @Column(name = "printed_date")
    private Date printedDate;

    @Column(name = "print_label")
    private String printLabel;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "department")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "staff_type")
    private StaffType staffType;

    public String getFullName() {
        String first = (firstName != null && !firstName.trim().isEmpty()) ? firstName.trim() : "";
        String last = (lastName != null && !lastName.trim().isEmpty()) ? lastName.trim() : "";
        String result = (first + " " + last).trim();
        return result.isEmpty() ? "Unknown Employee" : result;
    }
    
    // Alias for getFullName to maintain backward compatibility
    public String getDisplayName() {
        return getFullName();
    }
}
