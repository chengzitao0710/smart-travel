package com.smarttravel.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智游天下 - 智慧旅游平台 API 文档")
                        .version("1.0.0")
                        .description("智游天下智慧旅游平台接口文档，包含用户、景点、门票、游记、社交、路线、轨迹等模块")
                        .contact(new Contact()
                                .name("智游天下团队")));
    }
}