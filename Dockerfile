# Stage 1: Build the application using Maven
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy only the pom.xml first to leverage Docker caching for dependencies
COPY pom.xml .
# Copy the rest of the project files
COPY src ./src

# Download dependencies (this step will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B


# Run Maven to build the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar product.jar

# Expose the application port
EXPOSE 8080

# Install netcat for the entrypoint script to wait for RabbitMQ and PostgreSQL
RUN apk add --no-cache netcat-openbsd

# Default command to run the application
CMD ["java", "-jar", "product.jar"]