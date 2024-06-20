# Build a java application image based on openjdk 17 and run it on port 8080
FROM maven:3.8.1-openjdk-17 as build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Use openjdk:17 as the base image to run the application
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/copilot-demo-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]