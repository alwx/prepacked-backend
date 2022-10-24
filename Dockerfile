FROM eclipse-temurin:19-jdk-alpine
RUN mkdir /opt/app
WORKDIR /opt/app
COPY ./projects/backend/target/backend.jar .
CMD ["java", "-jar", "backend.jar"]