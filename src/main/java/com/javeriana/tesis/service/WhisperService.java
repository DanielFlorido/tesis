package com.javeriana.tesis.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.javeriana.tesis.model.TranscriptionTask;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class WhisperService {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/Desktop/audios";
    private static final String SCRIPT_PATH = "C:\\\\Users\\\\JAMES MORIARTY\\\\Desktop\\\\WhisperTranscriptor\\\\Transcriptor.py";

    private final BlockingQueue<TranscriptionTask> queue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        // Hilo que consume tareas de la cola
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    TranscriptionTask task = queue.take();
                    String result = ejecutarWhisperTranscription(task.getFilePath());
                    task.getResultFuture().complete(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public ResponseEntity<String> transcribirAudio(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try {
            // Asegura que el directorio exista
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Guarda el archivo
            Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            Files.write(filePath, file.getBytes());

            // Llama a la transcripción
            String resultado = ejecutarWhisperTranscription(filePath);
            return ResponseEntity.ok(resultado);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo");
        }
    }

    private String ejecutarWhisperTranscription(Path path) {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder("python", SCRIPT_PATH, path.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader stdError = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = stdInput.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = stdError.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            System.out.println("Errores (si los hay):");
            System.out.println(errorOutput.toString());
            System.out.println("El script terminó con código: " + exitCode);
            System.out.println("Salida del script: " + output.toString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return output.toString();
    }
}
