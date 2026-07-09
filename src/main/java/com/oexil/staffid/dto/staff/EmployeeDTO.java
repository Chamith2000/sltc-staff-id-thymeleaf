package com.oexil.staffid.dto.staff;

import com.oexil.staffid.constants.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
@Accessors(chain = true)
public class EmployeeDTO {

    private Long id;

    private String firstName;

    private String lastName;


    private String title;

    @NotBlank(message = "Employee No is required")
    private String empNo;

    private String nic;

    @NotBlank(message = "Designation is required")
    private String designation;

    private String qualification;

    private DepartmentDTO department;
    private Long departmentId;
    private String departmentDisplayName;

    private StaffTypeDTO staffType;
    private Long staffTypeId;
    private String staffTypeDisplayName;

    private FacultyDTO faculty;
    private Long facultyId;
    private String facultyDisplayName;

    private String email;

    private String address;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    private Date createDate;
    private Date issueDate;
    
    private Boolean printed;
    private Date printedDate;
    private String printLabel;
    private String batchId;

    private MultipartFile image;

    @Setter(AccessLevel.NONE)
    private String filePath;
    private String fileName;

    private byte[] qr;
    private String qrBase64;

    private String profileImageBase64;

    public void setFilePath(String filePath) {
        this.filePath = (filePath != null) ? Constants.IMAGE_URL + filePath : null;  // or provide a default path
    }

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

    public String getPrintedName() {
        String fullName = getFullName();
        if (title != null && !title.trim().isEmpty() && 
           (title.trim().equals("Dr.") || title.trim().equals("Prof."))) {
            return title.trim() + " " + fullName;
        }
        return fullName;
    }
}
