# 실행 전용 이미지. jar는 GitHub Actions(cd.yml)에서 ./gradlew bootJar로 미리 빌드한다.
FROM eclipse-temurin:21-jre

LABEL org.opencontainers.image.source="https://github.com/MJU-hackathon/hackathon-server"

WORKDIR /app
COPY build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
