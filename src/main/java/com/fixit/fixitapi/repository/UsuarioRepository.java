package com.fixit.fixitapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByFaceId(String faceId);

    boolean existsByCorreo(String correo);
}