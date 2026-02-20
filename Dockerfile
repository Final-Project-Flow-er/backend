## 1. 빌드 스테이지 시작
# Gradle 자바 17 버전을 사용하여 빌드 환경 구축
FROM gradle:8.5-jdk17-corretto AS build

# 컨테이너 내부의 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 현재 디렉토리의 모든 파일을 복사
COPY . .

# 빌드 실행 (테스트 제외)
RUN gradle clean build -x test --no-daemon

## 2. 실행 스테이지 시작
FROM amazoncorretto:17

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar ./

# plain이 붙지 않은 실행 가능한 JAR 파일을 app.jar로 변경
RUN mv $(ls *.jar | grep -v plain) app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]