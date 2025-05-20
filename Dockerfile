# Use an official OpenJDK image as a base
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files
COPY pom.xml ./
COPY src ./src

# Package the application (requires Maven)
RUN apt-get update && apt-get install -y maven && \
    mvn -B package -DskipTests

# Run the Spring Boot app
CMD ["java", "-jar", "target/testing-center-reservation-system-1.0-SNAPSHOT.jar"]
