package com.chaing.api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("발주(Order)")
                .pathsToMatch("/api/v1/orders/**")
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
