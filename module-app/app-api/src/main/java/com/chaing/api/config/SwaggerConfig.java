package com.chaing.api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi franchiseOrderApi() {
        return GroupedOpenApi.builder()
                .group("가맹점 발주(Order)")
                .pathsToMatch("/api/v1/franchise/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi headOfficeOrderApi() {
        return GroupedOpenApi.builder()
                .group("본사 발주(Head Office Order)")
                .pathsToMatch("/api/v1/head-office/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi returnApi() {
        return GroupedOpenApi.builder()
                .group("반품(Return)")
                .pathsToMatch("/api/v1/franchise/returns/**")
                .build();
    }

    @Bean
    public GroupedOpenApi salesApi() {
        return GroupedOpenApi.builder()
                .group("판매(Sales)")
                .pathsToMatch("/api/v1/franchise/sales/**")
                .build();
    }
}
