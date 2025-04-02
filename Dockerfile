# Use an Alpine-based OpenJDK image
FROM openjdk:17-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar product.jar

# Expose the application port
EXPOSE 8080

# Install netcat for the entrypoint script to wait for RabbitMQ and PostgreSQL
RUN apk add --no-cache netcat-openbsd

# Default command to run the application
CMD ["java", "-jar", "product.jar"]