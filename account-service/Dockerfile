# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application and skip tests to speed up the process
RUN mvn clean package -DskipTests

# --- Stage 2: Run the application ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file from the build stage
# Note: Adjust 'app-0.0.1-SNAPSHOT.jar' if your JAR name differs in pom.xml
COPY --from=build /app/target/*.jar app.jar

# Expose the port Spring Boot runs on by default
EXPOSE 8002

# Command to execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
