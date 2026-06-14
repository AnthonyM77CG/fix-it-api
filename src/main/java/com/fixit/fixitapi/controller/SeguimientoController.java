package com.fixit.fixitapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.dto.SeguimientoResponse;
import com.fixit.fixitapi.service.SeguimientoIncidenciaService;

@RestController
@RequestMapping
public class SeguimientoController {
    private SeguimientoIncidenciaService seguimientoService;

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> consultarHistorialIncidencia(@PathVariable Long id) {
        try {
            List<SeguimientoResponse> historial = seguimientoService.obtenerHistorialPorIncidencia(id);

            if (historial.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al recuperar la línea de tiempo de la incidencia."));
        }
    }
}
