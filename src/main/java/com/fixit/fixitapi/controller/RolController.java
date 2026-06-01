package com.fixit.fixitapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.repository.RolRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {
    private final RolRepository rolRepository;

    @GetMapping
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(rolRepository.findAll());
    }
}
