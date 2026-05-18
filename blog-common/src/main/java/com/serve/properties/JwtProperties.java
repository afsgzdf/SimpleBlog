package com.serve.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("blog.jwt")
@Data
public class JwtProperties {

    private String secretKey;
    private Long ttl;
    private String token;
}
