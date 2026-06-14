package com.fixit.fixitapi.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.fixit.fixitapi.dto.SeguimientoResponse;
import com.fixit.fixitapi.model.SeguimientoIncidencia;
import com.fixit.fixitapi.repository.SeguimientoIncidenciaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeguimientoIncidenciaService {

    private SeguimientoIncidenciaRepository seguimientoRepository;

    public List<SeguimientoResponse> obtenerHistorialPorIncidencia(Long incidenciaId) {

        List<SeguimientoIncidencia> seguimientos = seguimientoRepository
                .findByIncidenciaIdOrderByFechaAsc(incidenciaId);

        return seguimientos.stream().map(seg -> {
            SeguimientoResponse dto = new SeguimientoResponse();
            dto.setEstado(seg.getEstado().getNombre());
            dto.setFecha(seg.getFecha());
            dto.setComentario(seg.getComentario());

            List<String> urls = seg.getImagenesEvidencia().stream()
                    .map(img -> img.getUrl())
                    .collect(Collectors.toList());

            dto.setImagenes(urls);
            return dto;
        }).collect(Collectors.toList());
    }
}
