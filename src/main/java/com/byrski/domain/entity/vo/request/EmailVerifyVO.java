package com.byrski.domain.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmailVerifyVO implements Serializable {

    @NotNull
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "(register|reset)")
    private String type;
}
