FROM eclipse-temurin:17-jdk
VOLUME /tmp
COPY target/*.jar gateway.jar
ENTRYPOINT ["java","-Dspring.profiles.active=stg","-jar","/gateway.jar"]