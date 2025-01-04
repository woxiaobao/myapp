FROM openjdk:8
WORKDIR /app
COPY ./target/myapp.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "myapp.jar"]