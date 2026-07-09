package com.oexil.staffid.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
