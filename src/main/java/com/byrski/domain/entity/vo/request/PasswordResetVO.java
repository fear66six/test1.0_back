package com.byrski.domain.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PasswordResetVO implements Serializable {
    @Email
    @NotNull
    String email;
    @Length(max = 6, min = 6)
    String code;
    @Length(min = 6, max = 30)
    String password;
}
