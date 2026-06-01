package com.fixit.fixitapi.dto;

import lombok.Data;

@Data
public class CorreoLoginRequest {
    private String correo;
    private String contraseña;
}
