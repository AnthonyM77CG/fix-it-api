package com.fixit.fixitapi;

import com.fixit.fixitapi.service.RekognitionService;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class FixitapiApplication implements CommandLineRunner {

	private final RekognitionService rekognitionService;

	public static void main(String[] args) {
		SpringApplication.run(FixitapiApplication.class, args);
	}

	@Override
	public void run(String... args) {
		rekognitionService.inicializarColeccion();
	}
}
