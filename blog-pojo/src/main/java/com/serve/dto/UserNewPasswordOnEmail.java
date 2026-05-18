package com.serve.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserNewPasswordOnEmail implements Serializable {
    private Long id;
    private String email;
    private String newPassword;
    private String repeatPassword;
    private String code;
}
