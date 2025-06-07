FROM maven:3.9.9-eclipse-temurin-21-jammy AS builder
WORKDIR /app
COPY pom.xml ./
COPY src ./src/
RUN mvn clean package -DskipTests
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/tesis-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
