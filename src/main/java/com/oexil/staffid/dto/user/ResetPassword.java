package com.oexil.staffid.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class ResetPassword {

    @NotNull
    @NotBlank
    private String resetCode;

    @NotNull
    @NotBlank
    private String newPassword;

    @NotNull
    @NotBlank
    private String confirmPassword;
}