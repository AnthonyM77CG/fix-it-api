package com.fixit.fixitapi.controller;

import com.fixit.fixitapi.service.TipoIncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tipos")
@RequiredArgsConstructor
public class TipoIncidenciaController {

    private final TipoIncidenciaService tipoService;

    @GetMapping
    public ResponseEntity<?> getTipos() {
        return ResponseEntity.ok(tipoService.obtenerTodos());
    }
}
