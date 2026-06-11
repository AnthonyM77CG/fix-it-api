package com.fixit.fixitapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.ImagenIncidencia;
import com.fixit.fixitapi.model.Incidencia;

public interface ImagenIncidenciaRepository extends JpaRepository<ImagenIncidencia, Long> {
    List<ImagenIncidencia> findByIncidencia(Incidencia incidencia);
}
