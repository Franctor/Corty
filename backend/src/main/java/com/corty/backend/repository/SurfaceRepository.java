package com.corty.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.Surface;

@Repository
public interface SurfaceRepository extends JpaRepository<Surface, Long> {

    boolean existsByNameIgnoreCase(String name);
}
