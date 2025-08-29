# Use Maven image instead of just OpenJDK
FROM maven:3.9.4-eclipse-temurin-17

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 8082

# Run the application
CMD ["java", "-jar", "target/pomodoro-service-0.0.1-SNAPSHOT.jar"]