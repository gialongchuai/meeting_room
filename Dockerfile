# Stage 1: Build application using Maven and JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image using lightweight JRE 21 Alpine
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root system user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create logs directory with proper ownership
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Copy built artifact from builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# Healthcheck using built-in wget in Alpine's busybox
HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
