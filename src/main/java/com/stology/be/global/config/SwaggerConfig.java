package com.stology.be.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info().title("UMC10th").description("10기 Swagger").version("0.0.1");

        // JWT 토큰 헤더 방식
        String securityScheme = "JWT TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);

        Components components = new Components()
                .addSecuritySchemes(securityScheme, new SecurityScheme()
                        .name(securityScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01. auth")
                .displayName("01. 인증 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/auth/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi uploadApi() {
        return GroupedOpenApi.builder()
                .group("02. upload")
                .displayName("02. 스터디 자료 업로드 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch(
                        "/api/study/{studyId}/upload",
                        "/api/study/{studyId}/uploadSSE",
                        "/api/study/{studyId}/analyze",
                        "/api/study/{studyId}/studyMaterial/{studyMaterialId}/summary",
                        "/api/study/{studyId}/studyMaterial/{studyMaterialId}/upload") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi weekRecordApi() {
        return GroupedOpenApi.builder()
                .group("03. weekRecord")
                .displayName("03. 주차별 기록 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch(
                        "/api/study/{studyId}/node/{nodeId}/info",
                        "/api/study/{studyId}/active-nodes") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi notionNodeApi() {
        return GroupedOpenApi.builder()
                .group("04. notionNode")
                .displayName("04. 개념 노드 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch(
                        "/api/study/{studyId}/accept-node",
                        "/api/study/{studyId}/node/get-examination-info") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }


    @Bean
    public GroupedOpenApi homeTaskApi() {
        return GroupedOpenApi.builder()
                .group("05. homeTask")
                .displayName("05. 홈 화면 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch(
                        "/api/home/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }


    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00. ALL")
                .displayName("00. 전체 API")
                .pathsToMatch("/**") // 모든 경로 포함
                .build();
    }



}
