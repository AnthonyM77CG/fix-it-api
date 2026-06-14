package com.fixit.fixitapi.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fixit.fixitapi.dto.IncidenciaRequest;
import com.fixit.fixitapi.dto.IncidenciaResponse;
import com.fixit.fixitapi.dto.SeguimientoRequest;
import com.fixit.fixitapi.dto.SeguimientoResponse;
import com.fixit.fixitapi.model.EstadoIncidencia;
import com.fixit.fixitapi.model.ImagenIncidencia;
import com.fixit.fixitapi.model.Incidencia;
import com.fixit.fixitapi.model.SeguimientoIncidencia;
import com.fixit.fixitapi.model.TipoIncidencia;
import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.ImagenIncidenciaRepository;
import com.fixit.fixitapi.repository.IncidenciaRepository;
import com.fixit.fixitapi.repository.SeguimientoIncidenciaRepository;
import com.fixit.fixitapi.repository.TipoIncidenciaRepository;
import com.fixit.fixitapi.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
        private final IncidenciaRepository incidenciaRepository;
        private final SeguimientoIncidenciaRepository seguimientoRepository;
        private final ImagenIncidenciaRepository imagenRepository;
        private final TipoIncidenciaRepository tipoRepository;
        private final EstadoIncidenciaService estadoService;
        private final UsuarioRepository usuarioRepository;
        private final S3Service s3Service;

        public List<IncidenciaResponse> obtenerTodas() {
                return incidenciaRepository.findAllByOrderByFechaAperturaDesc()
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public Map<String, Object> obtenerDashboard() {
                List<Incidencia> todas = incidenciaRepository.findAll();

                long total = todas.size();
                long pendientes = todas.stream().filter(i -> i.getEstado().getNombre().equals("PENDIENTE")).count();
                long enProceso = todas.stream().filter(i -> i.getEstado().getNombre().equals("EN_PROCESO")).count();
                long resueltas = todas.stream().filter(i -> i.getEstado().getNombre().equals("RESUELTO")).count();

                // Incidencias por área
                Map<String, Long> porArea = todas.stream()
                                .collect(Collectors.groupingBy(i -> i.getArea().getNombre(), Collectors.counting()));

                // Últimas 5 incidencias
                List<IncidenciaResponse> recientes = todas.stream()
                                .sorted((a, b) -> b.getFechaApertura().compareTo(a.getFechaApertura()))
                                .limit(5)
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("total", total);
                dashboard.put("pendientes", pendientes);
                dashboard.put("enProceso", enProceso);
                dashboard.put("resueltas", resueltas);
                dashboard.put("porArea", porArea);
                dashboard.put("recientes", recientes);

                return dashboard;
        }

        public IncidenciaResponse crearIncidencia(IncidenciaRequest request, Usuario empleado) {

                TipoIncidencia tipo = tipoRepository.findById(request.getTipoId())
                                .orElseThrow(() -> new RuntimeException("Tipo de incidencia no encontrado"));
                EstadoIncidencia estadoPendiente = estadoService.buscarPorNombre("PENDIENTE");

                Incidencia incidencia = new Incidencia();
                incidencia.setEmpleado(empleado);
                incidencia.setArea(empleado.getArea());
                incidencia.setTipo(tipo);
                incidencia.setPrioridad(request.getPrioridad());
                incidencia.setDetalle(request.getDetalle());
                incidencia.setEstado(estadoPendiente);
                incidencia.setFechaApertura(LocalDateTime.now());
                incidenciaRepository.save(incidencia);

                // Subir y guardar imágenes
                if (request.getImagenesBase64() != null && !request.getImagenesBase64().isEmpty()) {
                        for (String base64 : request.getImagenesBase64()) {
                                String url = s3Service.subirImagen(base64);
                                ImagenIncidencia imagen = new ImagenIncidencia();
                                imagen.setIncidencia(incidencia);
                                imagen.setSeguimiento(null);
                                imagen.setUrl(url);
                                imagenRepository.save(imagen);
                        }
                }

                // Primer seguimiento automático
                SeguimientoIncidencia seguimiento = new SeguimientoIncidencia();
                seguimiento.setIncidencia(incidencia);
                seguimiento.setEstado(estadoPendiente);
                seguimiento.setFecha(LocalDateTime.now());
                seguimiento.setComentario("Incidencia creada");
                seguimientoRepository.save(seguimiento);
                return mapToResponse(incidencia);
        }

        public IncidenciaResponse asignarTecnico(Long incidenciaId, Long tecnicoId) {
                Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

                Usuario tecnico = usuarioRepository.findById(tecnicoId)
                                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

                incidencia.setTecnico(tecnico);
                incidenciaRepository.save(incidencia);

                return mapToResponse(incidencia);
        }

        public List<IncidenciaResponse> obtenerPorEmpleado(Usuario empleado) {
                return incidenciaRepository
                                .findByEmpleadoOrderByFechaAperturaDesc(empleado)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<IncidenciaResponse> obtenerPorTecnico(Usuario tecnico) {
                return incidenciaRepository
                                .findByTecnicoOrderByFechaAperturaDesc(tecnico)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public IncidenciaResponse obtenerPorId(Long id) {
                Incidencia incidencia = incidenciaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
                return mapToResponse(incidencia);
        }

        public IncidenciaResponse tomarIncidencia(Long incidenciaId) {
                Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

                EstadoIncidencia enProceso = estadoService.buscarPorNombre("EN_PROCESO");
                incidencia.setEstado(enProceso);
                incidenciaRepository.save(incidencia);

                SeguimientoIncidencia seguimiento = new SeguimientoIncidencia();
                seguimiento.setIncidencia(incidencia);
                seguimiento.setEstado(enProceso);
                seguimiento.setFecha(LocalDateTime.now());
                seguimiento.setComentario("Técnico tomó la incidencia");
                seguimientoRepository.save(seguimiento);

                return mapToResponse(incidencia);
        }

        private IncidenciaResponse mapToResponse(Incidencia inc) {
                List<SeguimientoIncidencia> seguimientos = seguimientoRepository.findByIncidenciaOrderByFechaAsc(inc);

                List<SeguimientoResponse> seguimientosDto = seguimientos.stream()
                                .map(s -> SeguimientoResponse.builder()
                                                .estado(s.getEstado().getNombre())
                                                .fecha(s.getFecha())
                                                .comentario(s.getComentario())
                                                .build())
                                .collect(Collectors.toList());

                List<String> urlsImagenes = imagenRepository.findByIncidencia(inc)
                                .stream()
                                .map(ImagenIncidencia::getUrl)
                                .collect(Collectors.toList());

                return IncidenciaResponse.builder()
                                .id(inc.getId())
                                .tipo(inc.getTipo().getNombre())
                                .prioridad(inc.getPrioridad())
                                .detalle(inc.getDetalle())
                                .imagenes(urlsImagenes)
                                .estado(inc.getEstado().getNombre())
                                .area(inc.getArea().getNombre())
                                .empleado(inc.getEmpleado().getNombre() + " " + inc.getEmpleado().getApellido())
                                .tecnico(inc.getTecnico() != null
                                                ? inc.getTecnico().getNombre() + " " + inc.getTecnico().getApellido()
                                                : null)
                                .fechaApertura(inc.getFechaApertura())
                                .fechaCierre(inc.getFechaCierre())
                                .seguimientos(seguimientosDto)
                                .build();
        }

        public IncidenciaResponse actualizarEstado(Long incidenciaId, SeguimientoRequest request) {
                Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));

                EstadoIncidencia nuevoEstado = estadoService.buscarPorNombre(request.getEstado());
                incidencia.setEstado(nuevoEstado);

                // Si se resuelve, registrar fecha de cierre
                if (request.getEstado().equals("RESUELTO")) {
                        incidencia.setFechaCierre(LocalDateTime.now());
                }

                // Subir imagen de evidencia si viene
                if (request.getImagenesBase64() != null && !request.getImagenesBase64().isEmpty()) {
                        for (String base64 : request.getImagenesBase64()) {
                                String url = s3Service.subirImagen(base64);
                                ImagenIncidencia imagen = new ImagenIncidencia();
                                imagen.setIncidencia(incidencia);
                                imagen.setUrl(url);
                                imagenRepository.save(imagen);
                        }
                }

                incidenciaRepository.save(incidencia);

                // Agregar seguimiento
                SeguimientoIncidencia seguimiento = new SeguimientoIncidencia();
                seguimiento.setIncidencia(incidencia);
                seguimiento.setEstado(nuevoEstado);
                seguimiento.setFecha(LocalDateTime.now());
                seguimiento.setComentario(request.getComentario());
                seguimientoRepository.save(seguimiento);

                return mapToResponse(incidencia);
        }
}
