package com.serve.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("blog.deepseek")
@Data
public class DeepSeekProperties {
    private String url;
    private String secretKey;
}
