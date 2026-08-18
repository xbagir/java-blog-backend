FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Warm the Gradle cache with the build scripts before copying sources
COPY settings.gradle build.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/blog-backend.jar /app/blog-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/blog-backend.jar"]