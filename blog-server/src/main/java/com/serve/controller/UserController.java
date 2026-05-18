package com.serve.controller;

import cn.hutool.core.bean.BeanUtil;
import com.serve.anno.RateLimitPerMinute;
import com.serve.anno.RateLimitPerSecond;
import com.serve.anno.RepeatSubmit;
import com.serve.dto.*;
import com.serve.exception.UserNotExistException;
import com.serve.po.User;
import com.serve.result.Result;
import com.serve.service.UserService;
import com.serve.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户相关操作")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @RateLimitPerMinute(permitsPerMinute = 6)
    @RepeatSubmit
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册");

        userService.register(userRegisterDTO);

        return Result.success();
    }

    @RateLimitPerMinute(permitsPerMinute = 1)
    @RepeatSubmit
    @Operation(summary = "向邮箱发送验证码")
    @GetMapping("/createCode/email")
    public Result createCodeByEmail(String email) {
        log.info("向邮箱发送验证码");
        //生成验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        userService.sendCodeToEmail(email, code);
        //设置验证码到redis缓存中并设置过期时间
        redisTemplate.opsForValue().set("code" + email, code, 60, TimeUnit.SECONDS);
        return Result.success();
    }

    @RateLimitPerSecond
    @RepeatSubmit
    @Operation(summary = "邮箱注册")
    @PostMapping("/code")
    public Result checkCode(@RequestBody UserRegisterEmailDTO userRegisterEmailDTO) {
        log.info("邮箱注册");

        userService.userRegisterByEmail(userRegisterEmailDTO);

        return Result.success();
    }

    @RateLimitPerSecond(permitsPerSecond = 1)
    @RepeatSubmit
    @Operation(summary = "用户登录")
    @PostMapping(value = "/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录");

        String token = userService.login(userLoginDTO);

        return Result.success(token);
    }

    @RateLimitPerSecond(permitsPerSecond = 1)
    @Operation(summary = "获取用户信息")
    @GetMapping
    public Result<User> getUsersMsg() {
        log.info("获取当前用户");
        User loginUserMsg = userService.getLoginUserMsg();
        return Result.success(loginUserMsg);
    }

    @RateLimitPerSecond(permitsPerSecond = 5)
    @Operation(summary = "根据id获取指定用户信息")
    @GetMapping("/user/{id}")
    public Result<UserVO> selectUserMsgById(@PathVariable Long id) {
        log.info("根据id获取指定用户信息");
        User user = userService.getById(id);

        if (user == null) {
            throw new UserNotExistException("用户不存在!");
        }

        return Result.success(BeanUtil.copyProperties(user, UserVO.class));
    }

    @Operation(summary = "设置新密码")
    @PostMapping("/password")
    public Result updateUserPwd(@RequestBody UserNewPasswordDTO userNewPasswordDTO,
                                @RequestHeader("Authorization") String token) {
        log.info("设置新密码");
        userService.updateUserPwd(userNewPasswordDTO);
        //删除redis缓存的token
        redisTemplate.delete(token);

        return Result.success();
    }

    @Operation(summary = "根据用户邮箱验证设置密码")
    @PostMapping("/password/email")
    public Result updatePwdByEmail(@RequestBody UserNewPasswordOnEmail userNewPasswordOnEmail,
                                   @RequestHeader("Authorization") String token) {
        log.info("根据用户邮箱验证设置密码");
        userService.updatePwdByEmail(userNewPasswordOnEmail);
        //删除redis缓存的token
        redisTemplate.delete(token);

        return Result.success();
    }

    @Operation(summary = "给用户注册邮箱")
    @PostMapping("/register/email")
    public Result registerEmail(@RequestBody UserRegisterEmailDTO userRegisterEmailDTO) {
        log.info("给用户注册邮箱");
        userService.registerEmailForUser(userRegisterEmailDTO);
        return Result.success();
    }

    @RepeatSubmit
    @Operation(summary = "退出登录")
    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        log.info("退出登录: {}", token);
        userService.userLogout(token);
        return Result.success();
    }
}
