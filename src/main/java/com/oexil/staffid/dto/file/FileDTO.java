package com.oexil.staffid.dto.file;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FileDTO {
    private String name, path;
}
