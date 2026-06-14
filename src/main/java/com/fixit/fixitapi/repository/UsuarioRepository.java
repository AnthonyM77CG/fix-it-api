package com.fixit.fixitapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByFaceId(String faceId);

    List<Usuario> findByRoleNombre(String nombre);

    boolean existsByCorreo(String correo);
}