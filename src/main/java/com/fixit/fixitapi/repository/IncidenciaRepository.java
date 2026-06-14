package com.fixit.fixitapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.EstadoIncidencia;
import com.fixit.fixitapi.model.Incidencia;
import com.fixit.fixitapi.model.Usuario;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByEmpleadoOrderByFechaAperturaDesc(Usuario empleado);

    List<Incidencia> findByTecnicoOrderByFechaAperturaDesc(Usuario tecnico);

    List<Incidencia> findAllByOrderByFechaAperturaDesc();

    List<Incidencia> findByEstadoNot(EstadoIncidencia estado);
}
