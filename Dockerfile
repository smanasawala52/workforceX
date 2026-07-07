# =====================
# Build stage
# =====================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/mvnw .

RUN chmod +x mvnw

COPY backend/src src

RUN ./mvnw clean package -DskipTests


# =====================
# Runtime stage
# =====================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]