# OpenJDK 17 버전의 이미지를 가져와 JVM 환경 구축
FROM eclipse-temurin:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너로 복사
COPY module-app/app-api/build/libs/*.jar app.jar

# JVM을 구동하여 스프링 부트 서버 애플리케이션 시작
ENTRYPOINT ["java", "-jar", "app.jar"]