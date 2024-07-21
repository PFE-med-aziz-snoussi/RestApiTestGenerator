# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS build

# Install Maven
RUN apt-get update && apt-get install -y wget && \
    wget https://downloads.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz && \
    tar xzf apache-maven-3.9.5-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.5/bin/mvn /usr/bin/mvn

WORKDIR /app

# Copy the pom.xml and src code
COPY pom.xml .
COPY src/ src/

# Download dependencies and build the application
RUN mvn dependency:go-offline
RUN mvn package -DskipTests

# Stage 2: Create the runtime container
FROM openjdk:17-jdk-slim

# Install Newman and Newman Reporter JSON
RUN apt-get update && apt-get install -y curl npm && \
    npm install -g newman newman-reporter-json

# Copy the JAR file from the builder stage
COPY --from=build /app/target/RestApiTestGenerator-0.0.1-SNAPSHOT.jar /RestApiTestGenerator-0.0.1-SNAPSHOT.jar

# Copy any required files
COPY public /public

EXPOSE 8081

# Set the entry point for the application with the specific JAR name
ENTRYPOINT ["java", "-jar", "/RestApiTestGenerator-0.0.1-SNAPSHOT.jar"]