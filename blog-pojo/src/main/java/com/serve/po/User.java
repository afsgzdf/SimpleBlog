package com.serve.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.serve.anno.Sensitive;
import com.serve.enums.SensitiveType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String nickname;

    private String username;

    @Sensitive(type = SensitiveType.MOBILE_PHONE)
    private String phone;

    @Sensitive(type = SensitiveType.EMAIL)
    private String email;

    @Sensitive(type = SensitiveType.PASSWORD)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
