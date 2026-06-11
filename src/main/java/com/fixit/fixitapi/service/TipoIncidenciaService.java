package com.fixit.fixitapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fixit.fixitapi.model.TipoIncidencia;
import com.fixit.fixitapi.repository.TipoIncidenciaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TipoIncidenciaService {
    private final TipoIncidenciaRepository tipoRepository;

    public List<TipoIncidencia> obtenerTodos() {
        return tipoRepository.findAll();
    }
}
