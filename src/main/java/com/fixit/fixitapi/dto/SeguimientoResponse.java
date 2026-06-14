package com.fixit.fixitapi.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeguimientoResponse {
    private String estado;
    private LocalDateTime fecha;
    private String comentario;
    private List<String> imagenes;
}
