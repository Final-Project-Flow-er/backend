# 1. 자바 버전
FROM amazoncorretto:17

# 2. 컨테이너 내부 작업 디렉토리 생성
WORKDIR /app

# 3. 빌드된 결과물을 컨테이너로 복사
COPY module-app/app-api/build/libs/*.jar app.jar

# 4. 컨테이너가 사용할 포트 명시
EXPOSE 8080

# 5. 실행 환경 설정
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]