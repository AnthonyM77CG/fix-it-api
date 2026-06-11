package com.fixit.fixitapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.service.EstadoIncidenciaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoIncidenciaController {

    private final EstadoIncidenciaService estadoService;

    @GetMapping
    public ResponseEntity<?> getEstados() {
        return ResponseEntity.ok(estadoService.obtenerTodos());
    }
}
