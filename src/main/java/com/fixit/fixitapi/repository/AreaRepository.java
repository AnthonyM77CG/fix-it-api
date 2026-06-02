package com.fixit.fixitapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fixit.fixitapi.model.Area;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findById(Long id);
}
