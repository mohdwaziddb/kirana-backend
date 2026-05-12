FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/kirana-0.0.1-SNAPSHOT.war app.war

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.war","--server.port=8081"]