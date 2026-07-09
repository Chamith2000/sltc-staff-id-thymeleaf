package com.oexil.staffid.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class ForgotPassword {

    @NotNull
    @NotBlank
    @Email
    @Pattern(regexp = ".*@sltc\\.ac\\.lk$", message = "Email must end with @sltc.ac.lk")
    private String email;
}