package com.fixit.fixitapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.EstadoIncidencia;

public interface EstadoIncidenciaRepository extends JpaRepository<EstadoIncidencia, Long> {
    Optional<EstadoIncidencia> findByNombre(String nombre);
}
