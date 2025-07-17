package com.hamss2.KINO.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;


@Configuration
public class SwaggerConfig {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    @Bean
    public OpenAPI openAPI() {
        // Access Token Bearer 인증 스키마 설정
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(accessTokenHeader);

//         SecurityRequirement 설정 - 각 토큰별 인증 요구사항 추가
        SecurityRequirement accessTokenRequirement = new SecurityRequirement().addList(accessTokenHeader);

        return new OpenAPI()
                .info(new Info()
                        .title("KINO")
                        .description("KINO REST API Document")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://43.203.218.183:8080").description("개발 서버 (배포)"),
                        new Server().url("http://localhost:8080").description("개발 서버 (localhost)")
                ))
                .components(new Components()
                        .addSecuritySchemes(accessTokenHeader, accessTokenScheme))
                .addSecurityItem(accessTokenRequirement);
    }

}

