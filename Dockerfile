# Stage 1: Build the WAR using Maven
FROM maven:3.9.3-eclipse-temurin-11 AS builder

# Set working directory
WORKDIR /app

# Copy only the pom first to leverage Docker cache
COPY pom.xml .
# Copy the source code
COPY src ./src
COPY WebContent ./WebContent
COPY bin ./bin

# Build the project (skip tests for speed)
RUN mvn clean package -DskipTests

# Stage 2: Prepare the runtime with Tomcat
FROM tomcat:9.0.85-jdk11

# Remove default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy the WAR from the builder stage
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
