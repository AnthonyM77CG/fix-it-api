package com.fixit.fixitapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private String area;
    private String similitud;
}