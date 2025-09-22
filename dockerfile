FROM openjdk:21
EXPOSE 8080
# Copiar el JAR generado al contenedor
ADD ./target/motum-holowits-camera-0.0.1-SNAPSHOT.jar app.jar
# Definir el comando de entrada
ENTRYPOINT [ "java", "-jar", "/app.jar" ]
