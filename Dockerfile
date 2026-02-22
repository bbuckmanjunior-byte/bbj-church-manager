FROM maven:3.8.1-openjdk-11 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY WebContent ./WebContent
COPY bin ./bin
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
RUN apt-get update && apt-get install -y wget bash unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/target/fresh_app-1.0.0.war ./target/
COPY --from=builder /app/bin/start.sh ./bin/
RUN chmod +x ./bin/start.sh

EXPOSE 8081
CMD ["bash", "./bin/start.sh"]
