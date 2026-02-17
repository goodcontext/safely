FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 5000