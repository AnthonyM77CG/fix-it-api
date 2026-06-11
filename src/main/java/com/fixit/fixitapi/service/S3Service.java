package com.fixit.fixitapi.service;

import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public String subirImagen(String imagenBase64) {
        // Limpiar prefijo si viene con data:image/...
        String base64Limpio = imagenBase64;
        if (imagenBase64.contains(",")) {
            base64Limpio = imagenBase64.split(",")[1];
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Limpio);
        String fileName = "incidencias/" + UUID.randomUUID() + ".jpg";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType("image/jpeg")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

        // Retorna la URL pública de la imagen
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
