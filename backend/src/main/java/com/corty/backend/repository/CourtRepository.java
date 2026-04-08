package com.corty.backend.repository;

import com.corty.backend.model.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    // Pistas cercanas ordenadas por distancia (fórmula Haversine en SQL nativo).
    // La distancia se devuelve en km. Filtra por deporte si sportName != null.
    @Query(value = """
            SELECT c.*, cl.name AS club_name,
                   (6371 * ACOS(
                       COS(RADIANS(:lat)) * COS(RADIANS(cl.geo_lat))
                       * COS(RADIANS(cl.geo_long) - RADIANS(:lon))
                       + SIN(RADIANS(:lat)) * SIN(RADIANS(cl.geo_lat))
                   )) AS distance_km
            FROM courts c
            JOIN clubs cl ON c.id_club = cl.id_club
            JOIN sports s ON c.id_sport = s.id_sport
            WHERE c.active = true
              AND cl.geo_lat IS NOT NULL
              AND cl.geo_long IS NOT NULL
              AND (:sportName IS NULL OR LOWER(s.name) = LOWER(:sportName))
            HAVING distance_km <= :radiusKm
            ORDER BY distance_km ASC
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> findNearbyCourtsRaw(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm,
            @Param("sportName") String sportName,
            @Param("limitCount") int limitCount
    );

    // Versión JPQL sin distancia — para cuando no se dispone de coordenadas del usuario
    @Query("""
            SELECT c FROM Court c
            JOIN FETCH c.club
            JOIN FETCH c.sport
            LEFT JOIN FETCH c.surface
            WHERE c.active = true
              AND (:sportName IS NULL OR LOWER(c.sport.name) = LOWER(:sportName))
            ORDER BY c.pricePerHour ASC
            """)
    List<Court> findActiveCourts(
            @Param("sportName") String sportName,
            org.springframework.data.domain.Pageable pageable
    );
}
