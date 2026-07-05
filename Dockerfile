# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and dependencies description
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execution rights on the Maven wrapper and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose standard Spring Boot / web port (Render overrides this with PORT env)
EXPOSE 8080
ENV PORT=8080

# Run the jar with Panama native access enabled (required by pgvector/spring ai vector store config)
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar", "--server.port=${PORT}"]
