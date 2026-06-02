package com.fixit.fixitapi.controller;

import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.UsuarioRepository;
import com.fixit.fixitapi.service.RekognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RekognitionService rekognitionService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        try {
            // Primero eliminar cara de Rekognition
            if (usuario.getFaceId() != null) {
                rekognitionService.eliminarCara(usuario.getFaceId());
                System.out.println("Cara eliminada de Rekognition: " + usuario.getFaceId());
            }

            // Luego eliminar de la BD
            usuarioRepository.delete(usuario);

            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}