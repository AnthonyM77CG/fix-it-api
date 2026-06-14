package com.fixit.fixitapi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.fixit.fixitapi.dto.UsuarioUpdateRequest;
import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final RekognitionService rekognitionService;
    private final UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> obtenerTecnicos() {
        return usuarioRepository.findByRoleNombre("Tecnico");
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFaceId() != null) {
            rekognitionService.eliminarCara(usuario.getFaceId());
        }

        usuarioRepository.delete(usuario);
    }

    public void actualizarPerfil(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            usuario.setCorreo(request.getCorreo());
        }
        if (request.getCelular() != null && !request.getCelular().isBlank()) {
            usuario.setCelular(request.getCelular());
        }

        usuarioRepository.save(usuario);
    }
}
