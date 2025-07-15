FROM openjdk:17-jdk-slim
VOLUME /tmp
ARG JAR_FILE=build/libs/KINO-*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

## Build stage
#FROM openjdk:17-jdk-slim AS build
#WORKDIR /app
#COPY . /app
#RUN ./gradlew build
## Runtime stage
#FROM openjdk:17-jre-slim AS runtime
#WORKDIR /app
#COPY --from=build /app/build/libs/KINO-0.0.1-SNAPSHOT.jar app.jar