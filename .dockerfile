FROM openjdk:21-jdk-alpine

WORKDIR /app
COPY target/emo-onnx-0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
