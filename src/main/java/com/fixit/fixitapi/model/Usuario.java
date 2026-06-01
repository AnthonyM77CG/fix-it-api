package com.fixit.fixitapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Rol role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contraseña;

    @Column(nullable = false, length = 9)
    private String celular;

    @Column(nullable = false, name = "face_id", unique = true, length = 100)
    private String faceId;

    @Column(name = "is_active")
    private boolean isActive = true;
}
