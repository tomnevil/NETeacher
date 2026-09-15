package com.neteacher.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI neteacherOpenApi() {
        return new OpenAPI().info(new Info()
                .title("NETeacher API")
                .description("中小学英语 AI 学习平台后端接口")
                .version("v1"));
    }
}
