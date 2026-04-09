package com.corty.backend.repository;

import com.corty.backend.model.Surface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurfaceRepository extends JpaRepository<Surface, Long> {
    boolean existsByNameIgnoreCase(String name);
}
