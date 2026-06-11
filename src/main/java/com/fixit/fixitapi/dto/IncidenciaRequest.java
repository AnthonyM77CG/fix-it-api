package com.fixit.fixitapi.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaRequest {
    private Long tipoId;
    private String prioridad;
    private String detalle;
    private List<String> imagenesBase64;
    private Long areaId;
}
