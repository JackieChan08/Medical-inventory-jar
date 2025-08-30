FROM openjdk:17-jdk-alpine
COPY Medical-inventory.jar .
ENTRYPOINT ["java", "-Xms1024m", "-Xmx1500m", "-jar", "Medical-inventory.jar"]
