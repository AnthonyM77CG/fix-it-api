package com.fixit.fixitapi.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.dto.AuthResponse;
import com.fixit.fixitapi.dto.CorreoLoginRequest;
import com.fixit.fixitapi.dto.FacialLoginRequest;
import com.fixit.fixitapi.dto.RegistroRequest;
import com.fixit.fixitapi.model.Area;
import com.fixit.fixitapi.model.Rol;
import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.UsuarioRepository;
import com.fixit.fixitapi.repository.AreaRepository;
import com.fixit.fixitapi.repository.RolRepository;
import com.fixit.fixitapi.service.JwtService;
import com.fixit.fixitapi.service.RekognitionService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.rekognition.model.InvalidParameterException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AutenticacionController {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final AreaRepository areaRepository;
        private final RekognitionService rekognitionService;
        private final JwtService jwtService;
        private final PasswordEncoder passwordEncoder;

        @PostMapping("/registro")
        public ResponseEntity<?> registerEmail(@RequestBody RegistroRequest request) {
                if (usuarioRepository.existsByCorreo(request.getCorreo())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of("error", "El correo ya está registrado"));
                }

                // Validar que existe el rol
                Rol rol = rolRepository.findById(request.getRoleId())
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

                // Validar que existe el área
                Area area = areaRepository.findById(request.getAreaId())
                                .orElseThrow(() -> new RuntimeException("Área no encontrada"));

                try {
                        // Verificar si la cara ya está registrada
                        RekognitionService.ResultadoBusqueda caraExistente = rekognitionService
                                        .buscarCara(request.getImagen());

                        if (caraExistente.encontrado()) {
                                return ResponseEntity.status(HttpStatus.CONFLICT)
                                                .body(Map.of("error", "Esta cara ya está registrada en el sistema"));
                        }
                        // Registrar cara en AWS Rekognition (obligatorio)
                        String faceId = rekognitionService.registrarCara(request.getImagen());

                        Usuario usuario = new Usuario();
                        usuario.setNombre(request.getNombre());
                        usuario.setApellido(request.getApellido());
                        usuario.setCorreo(request.getCorreo());
                        usuario.setContraseña(passwordEncoder.encode(request.getContraseña()));
                        usuario.setCelular(request.getCelular());
                        usuario.setRole(rol);
                        usuario.setArea(area);
                        usuario.setFaceId(faceId);
                        usuario.setActive(true);

                        usuarioRepository.save(usuario);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(Map.of("mensaje", "Usuario registrado correctamente"));

                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @PostMapping("/login/correo")
        public ResponseEntity<?> loginEmail(@RequestBody CorreoLoginRequest request) {

                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(request.getCorreo());

                if (usuarioOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Credenciales incorrectas"));
                }

                Usuario usuario = usuarioOpt.get();

                if (!usuario.isActive()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("error", "Cuenta desactivada"));
                }

                if (!passwordEncoder.matches(request.getContraseña(), usuario.getContraseña())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("error", "Credenciales incorrectas"));
                }

                String token = jwtService.generarToken(
                                usuario.getId().toString(),
                                usuario.getCorreo(),
                                usuario.getRole().getNombre());

                return ResponseEntity.ok(AuthResponse.builder()
                                .token(token)
                                .nombre(usuario.getNombre())
                                .apellido(usuario.getApellido())
                                .correo(usuario.getCorreo())
                                .rol(usuario.getRole().getNombre())
                                .area(usuario.getArea().getNombre())
                                .build());
        }

        @PostMapping("/login/facial")
        public ResponseEntity<?> loginFace(@RequestBody FacialLoginRequest request) {

                try {
                        RekognitionService.ResultadoBusqueda resultado = rekognitionService
                                        .buscarCara(request.getImagen());

                        if (resultado.sinCara()) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Map.of("error", "No se detectó ningún rostro en la imagen"));
                        }

                        if (!resultado.encontrado()) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of("error", "Cara no reconocida"));
                        }

                        Optional<Usuario> usuarioOpt = usuarioRepository.findByFaceId(resultado.faceId());

                        if (usuarioOpt.isEmpty()) {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                                .body(Map.of("error", "Cara no reconocida"));
                        }

                        Usuario usuario = usuarioOpt.get();

                        if (!usuario.isActive()) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body(Map.of("error", "Cuenta desactivada"));
                        }

                        String token = jwtService.generarToken(
                                        usuario.getId().toString(),
                                        usuario.getCorreo(),
                                        usuario.getRole().getNombre());

                        return ResponseEntity.ok(AuthResponse.builder()
                                        .token(token)
                                        .nombre(usuario.getNombre())
                                        .apellido(usuario.getApellido())
                                        .correo(usuario.getCorreo())
                                        .rol(usuario.getRole().getNombre())
                                        .area(usuario.getArea().getNombre())
                                        .similitud(String.format("%.1f%%", resultado.similitud()))
                                        .build());

                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", e.getMessage()));
                }

        }

        // Verifica si una cara ya está registrada sin hacer login
        @PostMapping("/verificar-cara")
        public ResponseEntity<?> verificarCara(@RequestBody FacialLoginRequest request) {
                try {
                        RekognitionService.ResultadoBusqueda resultado = rekognitionService
                                        .buscarCara(request.getImagen());

                        if (resultado.sinCara()) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Map.of(
                                                                "caraExistente", false,
                                                                "sinCara", true,
                                                                "error", "No se detectó ningún rostro en la imagen"));
                        }

                        return ResponseEntity.ok(Map.of(
                                        "caraExistente", resultado.encontrado(),
                                        "sinCara", false));

                } catch (InvalidParameterException e) {
                        // ✅ Captura específica cuando Rekognition no detecta cara
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of(
                                                        "caraExistente", false,
                                                        "sinCara", true,
                                                        "error", "No se detectó ningún rostro en la imagen"));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @DeleteMapping("/faces/all")
        public ResponseEntity<?> limpiarColeccion() {
                try {
                        rekognitionService.limpiarColeccion();
                        return ResponseEntity.ok(Map.of("mensaje", "Colección limpiada"));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of("error", e.getMessage()));
                }
        }
}