package com.fixit.fixitapi.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeguimientoResponse {
    private String estado;
    private LocalDateTime fecha;
    private String comentario;
}
