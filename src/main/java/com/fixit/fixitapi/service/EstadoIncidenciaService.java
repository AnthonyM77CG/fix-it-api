package com.fixit.fixitapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fixit.fixitapi.model.EstadoIncidencia;
import com.fixit.fixitapi.repository.EstadoIncidenciaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstadoIncidenciaService {

    private final EstadoIncidenciaRepository estadoRepository;

    public List<EstadoIncidencia> obtenerTodos() {
        return estadoRepository.findAll();
    }

    public EstadoIncidencia buscarPorNombre(String nombre) {
        return estadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + nombre));
    }
}
