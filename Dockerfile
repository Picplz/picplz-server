# Step 1: Build the application
FROM eclipse-temurin:17-jdk AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy Gradle wrapper and related files
COPY gradlew .
COPY gradle gradle

# Copy project dependency files
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src src

# Ensure the Gradle wrapper has execution rights
RUN chmod +x ./gradlew

# Build the Spring Boot application using Gradle
RUN sh ./gradlew bootJar -x test

# Step 2: Create a minimal image with only the JAR file
FROM eclipse-temurin:17-jre

# Set the working directory inside the container
WORKDIR /picplz

# Copy the built JAR file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8000 to the outside world
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/picplz/app.jar"]
