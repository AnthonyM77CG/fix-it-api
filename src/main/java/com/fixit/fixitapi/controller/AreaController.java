package com.fixit.fixitapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixit.fixitapi.repository.AreaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {
    private final AreaRepository areaRepository;

    @GetMapping
    public ResponseEntity<?> getAreas() {
        return ResponseEntity.ok(areaRepository.findAll());
    }
}
