# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ChainG (체인지) — a tteokbokki (Korean food) supply chain management system connecting HQ (본사), franchises (가맹점), and factories (공장). Built with Spring Boot 3.2.2, Java 21, multi-module Gradle.

## Build & Run Commands

```bash
# Build
./gradlew build
./gradlew clean build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests FranchiseOrderServiceTests

# Run a specific test method
./gradlew test --tests FranchiseOrderServiceTests.createOrder_GivenValidProduct_ShouldReturnOrderId

# Run tests for a specific module
./gradlew :domain-orders:test
./gradlew :app-api:test

# Code coverage
./gradlew jacocoTestReport       # per-module reports
./gradlew jacocoRootReport       # aggregate report (80% line coverage required)
```

Coverage excludes DTOs, configs, entities, repositories, enums, builders, and exceptions.

## Module Structure

```
module-core/                  # Shared JPA, QueryDSL, validation utilities
module-app/
  app-api/                    # Spring Boot entry point: controllers, facades, security, exception handling
module-domain/
  domain-orders/              # Franchise→HQ and HQ→Factory order management
  domain-returns/             # Return/refund flow
  domain-sales/               # Franchise sales tracking
  domain-inventories/         # Inventory management
  domain-inventorylogs/       # Inventory change audit logs
  domain-users/               # User accounts
  domain-businesses/          # Franchise and factory business entities
  domain-settlements/         # Settlement/payment processing
  domain-notices/             # Notices/announcements
  domain-notifications/       # Notification events
  domain-transports/          # Logistics and transport
  domain-products/            # Product catalog (24 tteokbokki milkit variants)
```

Each domain module follows the same internal layout: `entity/`, `repository/`, `service/`, `dto/`, `enums/`, `exception/`, `support/`.

## Architecture

**Layered architecture with Facade pattern:**

1. **Controllers** (`app-api/controller/hq/`, `franchise/`, `factory/`) — REST endpoints grouped by actor role
2. **Facades** (`app-api/facade/`) — coordinate multiple domain services for complex operations
3. **Domain Services** (`domain-*/service/`) — business logic per bounded context
4. **Repositories** (`domain-*/repository/`) — Spring Data JPA + QueryDSL
5. **Entities** (`domain-*/entity/`) — JPA-mapped domain objects

`app-api` depends on all domain modules and `module-core`. Domain modules do not depend on each other.

**Tech stack:** Spring Security + JWT, Spring Data Redis (cache/sessions), MariaDB (prod), H2 (tests), SpringDoc OpenAPI, JaCoCo, SonarQube.

**Config:** `application.yml` (base) + `application-secret.yml` (DB, Redis, JWT credentials — not committed).

## Testing Conventions

- **Naming:** `MethodName_Given<Context>_Should<ExpectedResult>`
- **Structure:** given / when / then (AAA pattern)
- **Controller tests:** `@WebMvcTest`, mock the service layer
- **Service tests:** `@SpringBootTest`, use real H2 DB (no mocks)
- **Repository tests:** `@DataJpaTest`
- Test classes mirror source package structure (e.g., `OrderService` → `OrderServiceTest` in the same package under `src/test/`)

## Commit Convention

Format: `[<prefix>] #<IssueNumber> <Description>`

Prefixes: `feat`, `fix`, `del`, `docs`, `refactor`, `chore`, `test`, `style`

Examples:
- `[feat] #11 구글 로그인 API 기능 구현`
- `[fix] #10 회원가입 비즈니스 로직 오류 수정`
- `[test] #20 로그인 API 테스트 코드 작성`

Branch naming: `feat/FLOW-<TicketNumber>-<domain>-<description>` (lowercase)

## Code Conventions

- **Packages:** lowercase only, no underscores
- **Classes:** UpperCamelCase nouns
- **Methods:** lowerCamelCase, start with a verb or preposition
- **Constants/Enums:** UPPER_SNAKE_CASE
- **Collections:** plural or type-suffixed names (`userList`, `userMap`)
- **DB tables:** lower_snake_case
- **URLs:** RESTful, no verbs, no trailing slash, hyphens not underscores, lowercase, no extensions
- **Arrays:** brackets after the type (`String[] args`, not `String args[]`)
- **long literals:** uppercase `L` suffix

## Domain Terminology (Korean → meaning)

- **발주 (balju):** order placed by franchise→HQ or HQ→factory
- **반품 (banpum):** return/refund request from franchise to HQ
- **가맹점 (gamenjeom):** franchise store
- **본사 (bonsa):** headquarters
- **공장 (gongjang):** factory
- **상품 (sangpum):** goods sold by franchises to consumers
- **제품 (jepum):** products in the factory warehouse

Order status (franchise→HQ): 대기 → 접수 → 부분접수 → 배송중 → 배송완료 | 취소 | 반려
Order status (HQ→factory): 대기 → 접수 → 배송중 → 배송완료 | 취소 | 반려