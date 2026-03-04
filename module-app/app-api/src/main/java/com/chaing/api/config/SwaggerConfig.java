package com.chaing.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                .pathsToMatch("/api/v1/hq/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi factoryOrderApi() {
        return GroupedOpenApi.builder()
                .group("공장 발주(Factory Order)")
                .pathsToMatch("/api/v1/factory/orders/**")
                .build();
    }

    @Bean
    public GroupedOpenApi franchiseReturnApi() {
        return GroupedOpenApi.builder()
                .group("가맹점 반품(Return)")
                .pathsToMatch("/api/v1/franchise/returns/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hqReturnApi() {
        return GroupedOpenApi.builder()
                .group("본사 반품(Return)")
                .pathsToMatch("/api/v1/hq/returns/**")
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
    public GroupedOpenApi businessUnitManagementApi() {
        return GroupedOpenApi.builder()
                .group("사업장 관리(Business Unit Management)")
                .pathsToMatch("/api/v1/hq/business-units/**")
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
    public GroupedOpenApi InternalTransportApi() {
        return GroupedOpenApi.builder()
                .group("내부 운송(Internal Transport")
                .pathsToMatch("/api/v1/transport/internal/**")
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

    @Bean
    public GroupedOpenApi transportVendorApi() {
        return GroupedOpenApi.builder()
                .group("운송 업체(Transport Vendor)")
                .pathsToMatch("/api/v1/transport/vendors/**")
                .build();
    }

    @Bean
    public GroupedOpenApi transportVehicleApi() {
        return GroupedOpenApi.builder()
                .group("운송 차량(Transport Vehicle)")
                .pathsToMatch("/api/v1/transport/vehicles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi noticeApi() {
        return GroupedOpenApi.builder()
                .group("공지사항(Notice)")
                .pathsToMatch("/api/v1/notices/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("알림(Notification)")
                .pathsToMatch("/api/v1/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hqInventoryLogApi() {
        return GroupedOpenApi.builder()
                .group("본사 로그(HqInventoryLog)")
                .pathsToMatch("/api/v1/hq/log/**")
                .build();
    }

    @Bean
    public GroupedOpenApi franchiseInventoryLogApi() {
        return GroupedOpenApi.builder()
                .group("가맹점 로그(FranchiseInventoryLog)")
                .pathsToMatch("/api/v1/franchise/log/**")
                .build();
    }

    @Bean
    public GroupedOpenApi factoryInventoryLogApi() {
        return GroupedOpenApi.builder()
                .group("공장 로그(FactoryInventoryLog)")
                .pathsToMatch("/api/v1/factory/log/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)   // HTTP 인증
                .scheme("bearer")                 // Bearer 토큰
                .bearerFormat("JWT")              // 표시용
                .in(SecurityScheme.In.HEADER);    // Authorization 헤더

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, securityScheme))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("AccountBookForMoms API")
                        .version("v1"));
    }
}
