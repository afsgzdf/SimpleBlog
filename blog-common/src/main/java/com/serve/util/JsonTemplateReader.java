package com.serve.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class JsonTemplateReader {

    public static String readJsonTemplate(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("json/" + fileName);
        try (
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            )
        ){
            return reader.lines().collect(Collectors.joining());
        }
    }
}
