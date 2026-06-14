package com.fixit.fixitapi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "seguimiento_incidencia")
public class SeguimientoIncidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoIncidencia estado;

    @OneToMany(mappedBy = "seguimiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImagenIncidencia> imagenesEvidencia = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column
    private String comentario;
}
