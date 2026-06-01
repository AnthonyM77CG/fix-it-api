package com.fixit.fixitapi.service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Face;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ListFacesResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;

@Service
@RequiredArgsConstructor
public class RekognitionService {
    private final RekognitionClient rekognitionClient;

    @Value("${aws.rekognition.collection-id}")
    private String collectionId;

    public void limpiarColeccion() {
        // Listar todas las caras
        ListFacesResponse response = rekognitionClient.listFaces(r -> r
                .collectionId(collectionId)
                .maxResults(100));

        List<String> faceIds = response.faces().stream()
                .map(Face::faceId)
                .collect(Collectors.toList());

        if (!faceIds.isEmpty()) {
            rekognitionClient.deleteFaces(r -> r
                    .collectionId(collectionId)
                    .faceIds(faceIds));
            System.out.println("✅ Eliminadas " + faceIds.size() + " caras");
        }
    }

    // Llama esto una vez al arrancar la app
    public void inicializarColeccion() {
        try {
            rekognitionClient.createCollection(r -> r.collectionId(collectionId));
            System.out.println("Colección creada: " + collectionId);
        } catch (ResourceAlreadyExistsException e) {
            System.out.println("Colección ya existe: " + collectionId);
        }
    }

    // Registrar cara → devuelve FaceId
    public String registrarCara(String imagenBase64) {
        byte[] imageBytes = Base64.getDecoder().decode(imagenBase64);

        IndexFacesResponse response = rekognitionClient.indexFaces(r -> r
                .collectionId(collectionId)
                .image(Image.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .maxFaces(1)
                .qualityFilter(QualityFilter.AUTO));

        List<FaceRecord> records = response.faceRecords();
        if (records.isEmpty()) {
            throw new RuntimeException("No se detectó ninguna cara en la imagen");
        }

        return records.get(0).face().faceId();
    }

    public record ResultadoBusqueda(boolean encontrado, boolean sinCara, String faceId, Float similitud) {
    }

    public ResultadoBusqueda buscarCara(String imagenBase64) {
        String base64Limpio = imagenBase64;
        if (imagenBase64.contains(",")) {
            base64Limpio = imagenBase64.split(",")[1];
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Limpio);

        try {
            SearchFacesByImageResponse response = rekognitionClient.searchFacesByImage(r -> r
                    .collectionId(collectionId)
                    .image(Image.builder()
                            .bytes(SdkBytes.fromByteArray(imageBytes))
                            .build())
                    .faceMatchThreshold(70f)
                    .maxFaces(1));

            List<FaceMatch> matches = response.faceMatches();
            if (matches.isEmpty()) {
                return new ResultadoBusqueda(false, false, null, null);
            }

            FaceMatch mejor = matches.get(0);
            return new ResultadoBusqueda(true, false, mejor.face().faceId(), mejor.similarity());

        } catch (RekognitionException e) {
            // ✅ Captura cualquier error de Rekognition
            System.out.println("RekognitionException: " + e.getMessage());
            if (e.getMessage().contains("There are no faces in the image")) {
                return new ResultadoBusqueda(false, true, null, null);
            }
            throw e; // si es otro error lo relanza
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            if (e.getMessage().contains("There are no faces in the image")) {
                return new ResultadoBusqueda(false, true, null, null);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    // Eliminar cara (para baja de usuario)
    public void eliminarCara(String faceId) {
        rekognitionClient.deleteFaces(r -> r
                .collectionId(collectionId)
                .faceIds(faceId));
    }

}
