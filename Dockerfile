## 1. 빌드 스테이지 시작
FROM gradle:8.11.1-jdk17-corretto AS build

WORKDIR /app

# 전체 소스 복사
COPY . .

# 빌드 실행 (전체 모듈 빌드)
RUN gradle clean build -x test --no-daemon

## 2. 실행 스테이지 시작
FROM amazoncorretto:17

WORKDIR /app

COPY --from=build /app/module-app/app-api/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]