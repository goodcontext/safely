# 1. Base Image: Java 21 (Temurin 배포판 권장)
FROM eclipse-temurin:21-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 JAR 파일을 Docker 이미지 안으로 복사
# Gradle 빌드 시 build/libs/ 안에 jar가 생김
COPY build/libs/*.jar app.jar

# 4. 운영 환경 프로필(prod) 적용
ENV SPRING_PROFILES_ACTIVE=prod

# 5. 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

# 6. 포트 노출 (AWS EB가 이 포트로 트래픽을 보냄)
EXPOSE 8080