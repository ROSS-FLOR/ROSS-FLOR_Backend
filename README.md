# Sistema de Gestión de Librería - Backend

Este proyecto es el backend para un sistema de gestión de librería, desarrollado con Spring Boot 3 y Java 17.

## Requisitos previos

- Java 17 (JDK) instalado.
- Maven instalado (o usar el wrapper si se genera).
- MySQL ejecutándose en el puerto 3306.
- Base de datos llamada `libreria_db` creada (o permitir que Spring la cree si el usuario tiene permisos).

## Configuración

El archivo de configuración se encuentra en `src/main/resources/application.properties`. Asegúrate de que las credenciales de la base de datos sean correctas:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/libreria_db...
spring.datasource.username=root
spring.datasource.password=root
```

## Compilación y Ejecución

### Opción 1: Ejecutar directamente (Desarrollo)
Para iniciar la aplicación sin generar un archivo JAR:
```bash
mvn spring-boot:run
```
La aplicación iniciará en `http://localhost:8080`.

### Opción 2: Compilar y Empaquetar (Producción)
Para generar el archivo ejecutable `.jar`:
```bash
mvn clean package
```
Esto generará un archivo en la carpeta `target/`, por ejemplo `backend-0.0.1-SNAPSHOT.jar`.

Para ejecutar ese archivo:
```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Documentación de API
Consulta el archivo `api_documentation.md` (o `/api/...` si configuraras Swagger) para ver los endpoints disponibles.
