FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY target/emo-onnx-0.1.jar app.jar

#RUN apt update && apt add g++ && apk add gcompat

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
