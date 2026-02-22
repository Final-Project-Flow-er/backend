# OpenJDK 17 버전의 이미지를 가져와 JVM 환경 구축
FROM openjdk:17-alpine

# 작업 디렉토리 설정
WORKDIR /app

COPY module-app/app-api/build/libs/*.jar app.jar

# 시간대 설정
RUN apk add --no-known-hosts tzdata && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

# app.jar를 리눅스 환경에서 실행하여 스프링 부트 서버 시작
ENTRYPOINT ["java", "-jar", "app.jar"]