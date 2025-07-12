 # Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as builder
RUN apt-get update && \
    apt-get install -y nodejs npm
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline
COPY package*.json ./
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Create the final, small image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
