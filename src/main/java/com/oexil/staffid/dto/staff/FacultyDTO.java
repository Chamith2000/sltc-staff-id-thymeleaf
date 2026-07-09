package com.oexil.staffid.dto.staff;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FacultyDTO {
    private Long id;
    private String name;
    private String displayName;
}
