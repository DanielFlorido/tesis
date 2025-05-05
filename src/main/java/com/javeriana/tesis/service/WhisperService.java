package com.javeriana.tesis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.javeriana.tesis.Dto.TranscriptionResponseDto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class WhisperService {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/Desktop/audios";
    private static final String SCRIPT_PATH = "C:\\\\Users\\\\JAMES MORIARTY\\\\Desktop\\\\WhisperTranscriptor\\\\Transcriptor.py";


    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${backend.transcription.url}")
    private String otroBackendUrl;

    public void enviarTranscripcion(TranscriptionResponseDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TranscriptionResponseDto> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(otroBackendUrl, request, String.class);
            System.out.println("Respuesta del otro backend: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Error al enviar al otro backend: " + e.getMessage());
        }
    }

    @Async
    public void procesarAsync(MultipartFile file, Long id, Path filePath) {
        try {
            String resultado = ejecutarWhisperTranscription(filePath);
            TranscriptionResponseDto dto = new TranscriptionResponseDto(id, filePath.toString(), resultado);
            enviarTranscripcion(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<String> transcribirAudio(MultipartFile file, Long id) {
        if (file.isEmpty() || id <= 0) {
            return ResponseEntity.badRequest().body("Archivo o ID inválido");
        }
    
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            Files.write(filePath, file.getBytes());
    
            // Procesamiento en segundo plano
            procesarAsync(file, id, filePath);
    
            // Respuesta inmediata
            return ResponseEntity.ok("Archivo recibido. Transcripción en proceso.");
    
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
