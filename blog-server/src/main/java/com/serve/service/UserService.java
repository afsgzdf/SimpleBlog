package com.serve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.serve.dto.*;
import com.serve.po.User;

public interface UserService extends IService<User> {

    void register(UserRegisterDTO userRegisterDTO);

    void sendCodeToEmail(String email, String code);

    void userRegisterByEmail(UserRegisterEmailDTO userRegisterEmailDTO);

    void saveUser(UserRegisterEmailDTO userRegisterEmailDTO);

    String login(UserLoginDTO userLoginDTO);

    User getLoginUserMsg();

    void updateUserPwd(UserNewPasswordDTO userNewPasswordDTO);

    void updatePwdByEmail(UserNewPasswordOnEmail userNewPasswordOnEmail);

    void registerEmailForUser(UserRegisterEmailDTO userRegisterEmailDTO);

    void userLogout(String token);
}
