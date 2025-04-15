package com.javeriana.tesis.controller;

import com.javeriana.tesis.service.WhisperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/whisper")
@CrossOrigin(origins = "http://localhost:4200")
@EnableAsync
public class WhisperController {

    @Autowired
    private WhisperService whisperService;

    @PostMapping("/transcription")
    public ResponseEntity<String> transcribirAudio(@RequestParam("formData") MultipartFile file) {
        return whisperService.transcribirAudio(file);
    }
}
