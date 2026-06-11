package com.fixit.fixitapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.Incidencia;
import com.fixit.fixitapi.model.SeguimientoIncidencia;

public interface SeguimientoIncidenciaRepository extends JpaRepository<SeguimientoIncidencia, Long> {
    List<SeguimientoIncidencia> findByIncidenciaOrderByFechaAsc(Incidencia incidencia);
}
