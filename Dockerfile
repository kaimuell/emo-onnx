FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/emo-onnx-0.1.jar app.jar

RUN apk update && apk add libstdc++

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
