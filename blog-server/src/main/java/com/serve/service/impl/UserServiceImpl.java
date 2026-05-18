package com.serve.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.serve.context.BaseContext;
import com.serve.dto.*;
import com.serve.exception.LoginFailedException;
import com.serve.exception.UserNotExistException;
import com.serve.mapper.UserMapper;
import com.serve.po.User;
import com.serve.properties.EmailProperties;
import com.serve.properties.JwtProperties;
import com.serve.service.UserService;
import com.serve.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtProperties jwtProperties;
    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;
    private final Executor cacheThreadPool;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {

        User user = new User();
        //1.查找用户名对应的账号是否存在
        User registerUser = lambdaQuery().eq(User::getUsername, userRegisterDTO.getUsername()).one();
        if (registerUser != null) {
            throw new LoginFailedException("账号已存在");
        }

        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getRepeatPassword())) {
            throw new LoginFailedException("两次输入的密码不正确!");
        }

        //2.若不存在，则把密码加密
        String encodePassword = passwordEncoder.encode(userRegisterDTO.getPassword());

        //3.插入账号
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(encodePassword);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);
    }

    @Override
    public void sendCodeToEmail(String email, String code) {
        cacheThreadPool.execute(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getUsername());
            message.setTo(email);
            message.setSubject("个人博客系统");
            message.setText("您的登录验证码:" + code + "\n1分钟内有效");
            //发送验证码
            javaMailSender.send(message);
        });
    }

    @Override
    public void userRegisterByEmail(UserRegisterEmailDTO userRegisterEmailDTO) {
        String key = "code" + userRegisterEmailDTO.getEmail();
        //得到redis缓存中的验证码，不可强转
        String realCode = String.valueOf(redisTemplate.opsForValue().get(key));
        if (realCode == null) {
            throw new RuntimeException("验证码不存在或过期!");
        }
        if (!realCode.equals(userRegisterEmailDTO.getCode())) {
            throw new RuntimeException("验证码不正确!");
        }

        if (!userRegisterEmailDTO.getPassword().equals(userRegisterEmailDTO.getRepeatPassword())) {
            throw new RuntimeException("两次输入的密码不正确!");
        }

        User user = new User();
        user.setEmail(userRegisterEmailDTO.getEmail());
        user.setUsername(userRegisterEmailDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterEmailDTO.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        save(user);
    }

    @Override
    public void saveUser(UserRegisterEmailDTO userRegisterEmailDTO) {
        User user = new User();
        user.setEmail(userRegisterEmailDTO.getEmail());
        user.setUsername(userRegisterEmailDTO.getEmail());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);
    }

    @Override
    public String login(UserLoginDTO userLoginDTO) {

        //查找用户是否存在
        User userForLogin = lambdaQuery().eq(User::getUsername, userLoginDTO.getUsername()).one();
        if (userForLogin == null || userForLogin.getPassword().isEmpty()) {
            throw new LoginFailedException("用户名或密码错误!");
        }

        //检测密码是否正确
        if (userLoginDTO.getPassword().isEmpty()) {
            throw new LoginFailedException("密码不能为空!");
        }

        boolean matches = passwordEncoder.matches(userLoginDTO.getPassword(), userForLogin.getPassword());
        if (!matches) {
            throw new LoginFailedException("用户名或密码错误!");
        }

        Map<String,Object> map = new HashMap<>();
        map.put("userId", userForLogin.getId());
        String jwtToken = JwtUtil.createJWTToken(jwtProperties.getSecretKey(), jwtProperties.getTtl(), map);
        //将token放入缓存
        redisTemplate.opsForValue().set(jwtToken, 1, jwtProperties.getTtl(), TimeUnit.MILLISECONDS);
        return jwtToken;
    }

    @Override
    public User getLoginUserMsg() {
        Long userId = BaseContext.getThreadLocal();
        User user = baseMapper.selectById(userId);
        return user;
    }

    @Override
    public void updateUserPwd(UserNewPasswordDTO userNewPasswordDTO) {
        //查询用户并校验
        Long userId = userNewPasswordDTO.getId();
        if (userId == null) {
            userId = BaseContext.getThreadLocal();
        }
        User user = getById(userId);
        String oldPassword = userNewPasswordDTO.getOldPassword();
        String newPassword = userNewPasswordDTO.getNewPassword();
        String repeatPassword = userNewPasswordDTO.getRepeatPassword();

        if (user == null) {
            throw new UserNotExistException("用户不存在!");
        }

        if (user.getPassword().isEmpty()) {
            throw new RuntimeException("密码未设置,请通过邮箱验证设置密码!");
        }

        boolean matches = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!matches) {
            throw new RuntimeException("用户密码错误!");
        }

        if (!newPassword.equals(repeatPassword)) {
            throw new RuntimeException("两次输入的密码不一致!");
        }

        lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getPassword, passwordEncoder.encode(newPassword))
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void updatePwdByEmail(UserNewPasswordOnEmail userNewPasswordOnEmail) {
        User user = getById(userNewPasswordOnEmail.getId());
        if (user == null) {
            throw new UserNotExistException("用户不存在!");
        }
        if (user.getEmail() == null || !user.getEmail().equals(userNewPasswordOnEmail.getEmail())) {
            throw new RuntimeException("用户邮箱不正确或未注册邮箱!");
        }
        if (!userNewPasswordOnEmail.getNewPassword().equals(userNewPasswordOnEmail.getRepeatPassword())) {
            throw new RuntimeException("两次输入的密码不正确!");
        }

        String key = "code" + userNewPasswordOnEmail.getEmail();
        //获取真实的验证码
        String realCode = String.valueOf(redisTemplate.opsForValue().get(key));
        if (realCode == null) {
            throw new RuntimeException("验证码不存在或过期!");
        }
        if (!realCode.equals(userNewPasswordOnEmail.getCode())) {
            throw new RuntimeException("验证码不正确!");
        }
        lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getPassword, passwordEncoder.encode(userNewPasswordOnEmail.getNewPassword()))
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void registerEmailForUser(UserRegisterEmailDTO userRegisterEmailDTO) {
        User user = getById(userRegisterEmailDTO.getId());
        if (user == null) {
            throw new UserNotExistException("用户不存在!");
        }
        if (user.getEmail() != null) {
            if (user.getEmail().equals(userRegisterEmailDTO.getEmail())) {
                throw new RuntimeException("用户邮箱已存在!");
            }
        }

        String key = "code" + userRegisterEmailDTO.getEmail();
        //获取真实的验证码
        String realCode = String.valueOf(redisTemplate.opsForValue().get(key));
        if (realCode == null) {
            throw new RuntimeException("验证码不存在或过期!");
        }
        if (!realCode.equals(userRegisterEmailDTO.getCode())) {
            throw new RuntimeException("验证码不正确!");
        }

        //注册邮箱给当前用户
        lambdaUpdate()
                .eq(User::getId, user.getId())
                .set(User::getEmail, userRegisterEmailDTO.getEmail())
                .set(User::getUpdateTime, LocalDateTime.now())
                .update();

        //删除缓存的验证码
        redisTemplate.delete(key);
    }

    @Override
    public void userLogout(String token) {
        redisTemplate.delete(token);
    }
}
