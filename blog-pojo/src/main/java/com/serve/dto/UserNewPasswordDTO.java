package com.serve.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserNewPasswordDTO implements Serializable {
    private Long id;
    private String oldPassword;
    private String newPassword;
    private String repeatPassword;
}
