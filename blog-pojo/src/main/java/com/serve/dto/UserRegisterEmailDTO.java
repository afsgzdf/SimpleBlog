package com.serve.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterEmailDTO {

    private Long id;

    @Email(message = "必须符合email格式!")
    private String email;
    private String code;

    private String password;
    private String repeatPassword;
}
