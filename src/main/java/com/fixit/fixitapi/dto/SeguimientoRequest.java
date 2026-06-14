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
public class SeguimientoRequest {
    private String estado;
    private String comentario;
    private List<String> imagenesBase64;
}
