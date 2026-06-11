package com.fixit.fixitapi.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.dto.IncidenciaRequest;
import com.fixit.fixitapi.dto.IncidenciaResponse;
import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.UsuarioRepository;
import com.fixit.fixitapi.service.IncidenciaService;
import com.fixit.fixitapi.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {
    private final IncidenciaService incidenciaService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> crearIncidencia(
            @RequestBody IncidenciaRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Usuario empleado = obtenerUsuarioDelToken(authHeader);
            IncidenciaResponse response = incidenciaService.crear(request, empleado);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mis-incidencias")
    public ResponseEntity<?> obtenerIncidencias(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Usuario empleado = obtenerUsuarioDelToken(authHeader);
            return ResponseEntity.ok(incidenciaService.obtenerPorEmpleado(empleado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> seguimientoIncidencia(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Helper para extraer usuario del token
    private Usuario obtenerUsuarioDelToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtService.extraerUserId(token);
        return usuarioRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
