package com.serve.interceptor;

import com.serve.context.BaseContext;
import com.serve.properties.JwtProperties;
import com.serve.result.Result;
import com.serve.util.JwtUtil;
import com.serve.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getToken());
        if (token != null) {
            //判断jwt是否有效
            String tokenState = stringRedisTemplate.opsForValue().get(token);

            if (tokenState == null) {
                ResponseUtil.loginExpired(response, new Result<>(401, "token无效或已过期!", null));
                return false;
            }

            Claims claims = JwtUtil.parseJWTToken(jwtProperties.getSecretKey(), token);
            Object userId = claims.get("userId");

            BaseContext.setThreadLocal(Long.valueOf(userId.toString()));

            return true;
        }
        ResponseUtil.loginExpired(response, new Result<>(401, "登录失败,请重新登录", null));

        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeThreadLocal();
    }
}
