FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# CAMBIO: Usamos 'openjdk:17-jre-slim' para la etapa de runtime.
# JRE es más pequeño que JDK, lo que resulta en un contenedor más ligero.
FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]