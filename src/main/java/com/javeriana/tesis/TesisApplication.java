package com.javeriana.tesis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TesisApplication {

	public static void main(String[] args) {
		SpringApplication.run(TesisApplication.class, args);
	}

}
