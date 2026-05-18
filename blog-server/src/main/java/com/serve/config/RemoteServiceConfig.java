package com.serve.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RemoteServiceConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)       //连接超时 3 秒
                .readTimeout(60, TimeUnit.SECONDS)       //读取 AI 响应超时 10 秒
                .writeTimeout(3, TimeUnit.MINUTES)
                //关闭自动重试
                .retryOnConnectionFailure(false)
                .build();
    }
}
