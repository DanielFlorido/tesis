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

import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.javeriana.tesis.Dto.TranscriptionResponseDto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

@Service
public class WhisperService {

    private static final String UPLOAD_DIR = "audios";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${backend.transcription.url}")
    private String otroBackendUrl;

    @Value("${whisper.microservice.url:http://python-microservice:8000/transcribe}")
    private String whisperMicroserviceUrl;

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
    public void procesarAsync(MultipartFile file, Long id, String filePath) {
        try {
            String resultado = enviarAudioAMicroservicio(file);
            System.out.println("Transcripción recibida: " + resultado); // <-- Agrega esto
            TranscriptionResponseDto dto = new TranscriptionResponseDto(id, filePath, resultado);
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
            procesarAsync(file, id, file.getOriginalFilename());

            // Respuesta inmediata
            return ResponseEntity.ok("Archivo recibido. Transcripción en proceso.");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo");
        }
    }

    private String enviarAudioAMicroservicio(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                whisperMicroserviceUrl, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("text");
        } else {
            throw new IOException("Error al transcribir audio: " + response.getStatusCode());
        }
    }
}