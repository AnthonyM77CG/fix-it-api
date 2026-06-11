package com.fixit.fixitapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.dto.IncidenciaRequest;
import com.fixit.fixitapi.dto.IncidenciaResponse;
import com.fixit.fixitapi.dto.SeguimientoResponse;
import com.fixit.fixitapi.model.Area;
import com.fixit.fixitapi.model.EstadoIncidencia;
import com.fixit.fixitapi.model.ImagenIncidencia;
import com.fixit.fixitapi.model.Incidencia;
import com.fixit.fixitapi.model.SeguimientoIncidencia;
import com.fixit.fixitapi.model.TipoIncidencia;
import com.fixit.fixitapi.model.Usuario;
import com.fixit.fixitapi.repository.AreaRepository;
import com.fixit.fixitapi.repository.ImagenIncidenciaRepository;
import com.fixit.fixitapi.repository.IncidenciaRepository;
import com.fixit.fixitapi.repository.SeguimientoIncidenciaRepository;
import com.fixit.fixitapi.repository.TipoIncidenciaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidencia")
@RequiredArgsConstructor
public class IncidenciaService {
    private final IncidenciaRepository incidenciaRepository;
    private final SeguimientoIncidenciaRepository seguimientoRepository;
    private final ImagenIncidenciaRepository imagenRepository;
    private final TipoIncidenciaRepository tipoRepository;
    private final AreaRepository areaRepository;
    private final EstadoIncidenciaService estadoService;
    private final S3Service s3Service;

    public IncidenciaResponse crear(IncidenciaRequest request, Usuario empleado) {
        Area area = areaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new RuntimeException("Área no encontrada"));

        TipoIncidencia tipo = tipoRepository.findById(request.getTipoId())
                .orElseThrow(() -> new RuntimeException("Tipo de incidencia no encontrado"));
        EstadoIncidencia estadoPendiente = estadoService.buscarPorNombre("PENDIENTE");

        Incidencia incidencia = new Incidencia();
        incidencia.setEmpleado(empleado);
        incidencia.setArea(area);
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

    public List<IncidenciaResponse> obtenerPorEmpleado(Usuario empleado) {
        return incidenciaRepository
                .findByEmpleadoOrderByFechaAperturaDesc(empleado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IncidenciaResponse obtenerPorId(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
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
}
