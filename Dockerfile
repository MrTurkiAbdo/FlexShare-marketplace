# Step 1: Build the Ktor application using Gradle and JDK 17
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :backend:installDist --no-daemon

RUN chmod +x gradlew

RUN ./gradlew :backend:installDist --no-daemon

# Step 2: Run the compiled executable on the official Eclipse Temurin Java runtime
FROM eclipse-temurin:17-jre-jammy
EXPOSE 8080
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app/bin
CMD ["./backend"]