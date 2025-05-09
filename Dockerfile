# ---------- Etapa 1: Construcción del JAR (opcional si ya tienes el JAR) ----------
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Etapa 2: Imagen final ----------
    FROM eclipse-temurin:17-jdk AS app

    # Crear directorio de trabajo
    WORKDIR /app
    
    # Copiar el JAR generado
    # Si usaste la etapa 1, usa esto:
    COPY --from=builder /app/target/*.jar app.jar
    # O si ya tienes el JAR construido localmente:
    #COPY target/app.jar app.jar
    
    # Copiar el script de transcripción
    COPY WhisperTranscriptor/Transcriptor.py /app/transcribir.py
    
    # Instalar Python y dependencias de Whisper
    RUN apt-get update && \
        apt-get install -y python3 python3-pip ffmpeg && \
        pip3 install --no-cache-dir openai-whisper
    
    # (Whisper instala automáticamente torch, numpy, etc.)
    
    # Exponer el puerto del Spring Boot (ajústalo si es diferente)
    EXPOSE 8080
    
    # Ejecutar la aplicación
    ENTRYPOINT ["java", "-jar", "app.jar"]
    