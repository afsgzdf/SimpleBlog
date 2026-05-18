package com.serve.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serve.util.JsonTemplateReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonTemplateConfig {

    private JsonNode jsonNode;
    private final ObjectMapper objectMapper;

    // 项目启动时执行一次
    @PostConstruct
    public void init() throws IOException {
        String template = JsonTemplateReader.readJsonTemplate("DeepSeek.json");
        jsonNode = objectMapper.readTree(template);
    }

    // 每次对话都从这里拿（复制一份，避免改到原模板）
    public JsonNode getJsonNode() {
        return jsonNode.deepCopy();
    }
}
