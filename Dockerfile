# --- Stage 1: Build ---
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Final ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/task2-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]