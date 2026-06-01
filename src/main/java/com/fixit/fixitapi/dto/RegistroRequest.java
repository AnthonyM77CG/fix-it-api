package com.fixit.fixitapi.dto;

import lombok.Data;

@Data
public class RegistroRequest {
    private String nombre;
    private String apellido;
    private String correo;
    private String contraseña;
    private String celular;
    private Long roleId;
    private Long areaId;
    private String imagen;
}
