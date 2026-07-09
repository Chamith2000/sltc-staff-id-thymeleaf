package com.oexil.staffid.dto.staff;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DepartmentDTO {
    private Long id;
    private String name;
    private String displayName;
    private StaffTypeDTO staffType;
    private FacultyDTO faculty;
}
