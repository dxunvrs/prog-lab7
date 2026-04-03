FROM gradle:9.3.1-jdk17 AS build
WORKDIR /app

COPY . .
RUN chmod +x gradlew

RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/server/build/libs/*.jar app.jar

EXPOSE 1234/udp

ENTRYPOINT ["java", "-jar", "app.jar"]