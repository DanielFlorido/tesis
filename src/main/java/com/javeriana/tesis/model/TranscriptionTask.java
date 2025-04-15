package com.javeriana.tesis.model;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;


public class TranscriptionTask {
    private Path filePath;
    private CompletableFuture<String> resultFuture;

    public TranscriptionTask(Path filePath) {
        this.filePath = filePath;
        this.resultFuture = new CompletableFuture<>();
    }

    public Path getFilePath() {
        return filePath;
    }

    public CompletableFuture<String> getResultFuture() {
        return resultFuture;
    }
}
