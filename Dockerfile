# Use Maven + Java 21 for build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Use lightweight JRE 21 for runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV OPENAI_API_KEY=${OPENAI_API_KEY}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
