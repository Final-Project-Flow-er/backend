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

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("인증(Auth)")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi myPageApi() {
        return GroupedOpenApi.builder()
                .group("마이페이지(MyPage)")
                .pathsToMatch("/api/v1/users/me/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userManagementApi() {
        return GroupedOpenApi.builder()
                .group("회원 관리(User Management)")
                .pathsToMatch("/api/v1/hq/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hqInventoryApi() {
        return GroupedOpenApi.builder()
                .group("본사 재고(HqInventory)")
                .pathsToMatch("/api/v1/hq/inventory/**")
                .build();
    }

    @Bean
    public GroupedOpenApi franchiseInventoryApi() {
        return GroupedOpenApi.builder()
                .group("가맹점 재고(FranchiseInventory)")
                .pathsToMatch("/api/v1/franchise/inventory/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hqProductApi() {
        return GroupedOpenApi.builder()
                .group("본사 상품(HqProduct)")
                .pathsToMatch("/api/v1/hq/product/**")
                .build();
    }

    @Bean
    public GroupedOpenApi franchiseProductApi() {
        return GroupedOpenApi.builder()
                .group("가맹점 상품(FranchiseProduct)")
                .pathsToMatch("/api/v1/franchise/product/**")
                .build();
    }
}
