package com.fixit.fixitapi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fixit.fixitapi.model.Rol;
import com.fixit.fixitapi.repository.RolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolService {
    private final RolRepository rolRepository;

    public List<Rol> obtenerTodos() {
        return rolRepository.findAll();
    }
}
