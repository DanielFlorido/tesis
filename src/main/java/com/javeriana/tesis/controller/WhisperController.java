package com.javeriana.tesis.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/whisper")
@CrossOrigin(origins = "http://localhost:4200")
public class WhisperController {
    
    private static final String uploadDir = System.getProperty("user.home") + "/Desktop/audios";

    //@Autowired
    //private WhisperService WhisperService;

    //http://localhost:8080/api/whisper/transcription
    @PostMapping("/transcription")
    public ResponseEntity<String> TranscribirAudio(@RequestParam("formData") MultipartFile file)
    {
        try{
            if(file.isEmpty())
            {
                return ResponseEntity.badRequest().body("El archivo esta vacio");
            }

            // Crear la carpeta si no existe
            Files.createDirectories(Paths.get(uploadDir));

            // Ruta de destino
            Path filePath = Paths.get(uploadDir, file.getOriginalFilename());

            Files.write(filePath, file.getBytes());

            String respuesta = "";

            respuesta = WhisperTranscription(filePath);

            return ResponseEntity.ok(respuesta);
        }catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo");
        } 
    }

    public String WhisperTranscription(Path path)
    {
        String transcripcion = "";

        String resultadoPython = "";

        try {
            // Ruta base definida
            String basePath = "C:\\\\Users\\\\JAMES MORIARTY\\\\Desktop\\\\WhisperTranscriptor\\\\Transcriptor.py";

            String filePath = path.toString();

            System.out.println("Inicio de la transcripción");

            ProcessBuilder pb = new ProcessBuilder("python", basePath, filePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Lee la salida del script de Python
            BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader stdError = new BufferedReader(
                new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // Variables para almacenar las salidas
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            String s;
            // Almacena la salida estándar
            while ((s = stdInput.readLine()) != null) {
                output.append(s).append("\n");
            }

            // Almacena los errores
            while ((s = stdError.readLine()) != null) {
                errorOutput.append(s).append("\n");
            }

            // Espera a que el proceso termine
            int exitCode = process.waitFor();

            System.out.println("Errores (si los hay):");
            System.out.println(errorOutput.toString());

            System.out.println("El script de Python terminó con el código: " + exitCode);

            // Puedes usar la salida almacenada en la variable `output` más adelante
            resultadoPython = output.toString();
            System.out.println("Salida del script de Python:");
            System.out.println("Resultado almacenado: " + resultadoPython);

        } catch (IOException e) {
            System.out.println("Excepción de E/S");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("El proceso fue interrumpido");
            e.printStackTrace();
        }

        transcripcion = resultadoPython;

        return transcripcion;
    }
}
