package com.fixit.fixitapi.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidenciaResponse {
    private Long id;
    private String tipo;
    private String prioridad;
    private String detalle;
    private List<String> imagenes;
    private String estado;
    private String area;
    private String empleado;
    private String tecnico;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private List<SeguimientoResponse> seguimientos;
}
