package com.javeriana.tesis.Dto;

public class TranscriptionResponseDto {
    private Long id;
    private String audioPath;
    private String transcription;
    
    public TranscriptionResponseDto(Long id, String audioPath, String transcription) {
        this.id = id;
        this.audioPath = audioPath;
        this.transcription = transcription;
    }
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public String getAudioPath() {
        return audioPath;
    }
    
    public String getTranscription() {
        return transcription;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
    
    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }
}
