package com.corty.backend.repository;

import com.corty.backend.model.Court;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

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
            LEFT JOIN surfaces su ON c.id_surface = su.id_surface
            WHERE c.active = true
              AND cl.geo_lat IS NOT NULL
              AND cl.geo_long IS NOT NULL
              AND (:sportName IS NULL OR LOWER(s.name) = LOWER(:sportName))
              AND (:surfaceName IS NULL OR (su.id_surface IS NOT NULL AND LOWER(su.name) = LOWER(:surfaceName)))
              AND (:coveredOnly = false OR c.covered = true)
              AND (:lightingOnly = false OR c.lighting = true)
              AND (:maxPrice IS NULL OR c.price_per_hour <= :maxPrice)
            HAVING distance_km <= :radiusKm
            ORDER BY
              IF(:sortBy = 'price',    IF(:sortDir = 'desc', -c.price_per_hour, c.price_per_hour), NULL) ASC,
              IF(:sortBy = 'distance', IF(:sortDir = 'desc', -distance_km,      distance_km),      NULL) ASC
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> findNearbyCourtsRaw(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm,
            @Param("sportName") String sportName,
            @Param("surfaceName") String surfaceName,
            @Param("coveredOnly") boolean coveredOnly,
            @Param("lightingOnly") boolean lightingOnly,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("limitCount") int limitCount
    );

    @Query("""
            SELECT c FROM Court c
            WHERE (:search IS NULL OR :search = ''
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.club.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.sport.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.club.name ASC, c.name ASC
            """)
    Page<Court> findAllFiltered(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT c FROM Court c
            WHERE c.club.organization.idOrganization = :orgId
              AND (:search IS NULL OR :search = ''
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.club.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.sport.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.club.name ASC, c.name ASC
            """)
    Page<Court> findAllFilteredByOrg(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);

    @Query("""
            SELECT c FROM Court c
            JOIN FETCH c.club cl
            JOIN FETCH c.sport sp
            LEFT JOIN FETCH c.surface su
            WHERE c.active = true
              AND (:sportName IS NULL OR LOWER(sp.name) = LOWER(:sportName))
              AND (:surfaceName IS NULL OR (su IS NOT NULL AND LOWER(su.name) = LOWER(:surfaceName)))
              AND (:coveredOnly = false OR c.covered = true)
              AND (:lightingOnly = false OR c.lighting = true)
              AND (:maxPrice IS NULL OR c.pricePerHour <= :maxPrice)
            """)
    List<Court> findActiveCourts(
            @Param("sportName") String sportName,
            @Param("surfaceName") String surfaceName,
            @Param("coveredOnly") boolean coveredOnly,
            @Param("lightingOnly") boolean lightingOnly,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            org.springframework.data.domain.Pageable pageable
    );
}
