package com.serve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Hello world!
 *
 */

@SpringBootApplication
@Slf4j
@EnableCaching
public class BlogApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(BlogApplication.class, args);
        log.info("server started...");
    }
}
