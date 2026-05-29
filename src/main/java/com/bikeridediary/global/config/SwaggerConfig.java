package com.bikeridediary.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger / OpenAPI 3.0 설정. 접근 경로: http://localhost:8080/swagger-ui.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("바라다 (BikeRideDiary) API")
                        .description("바이크 라이더를 위한 정비 기록 및 라이딩 코스 관리 앱 API")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Access Token. Format: Bearer {token}")));
    }
}
