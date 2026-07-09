package com.oexil.staffid.dto.staff;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StaffTypeDTO {
    private Long id;
    private String typeName;
    private String description;
}
