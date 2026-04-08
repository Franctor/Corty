package com.corty.backend.services;

import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.mapper.CourtMapper;
import com.corty.backend.model.Court;
import com.corty.backend.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtMapper courtMapper;

    private static final double DEFAULT_RADIUS_KM = 20.0;
    private static final int DEFAULT_LIMIT = 10;

    public List<NearbyCourtResponse> getNearbyCourts(Double lat, Double lon, String sport) {

        // Si el usuario proporciona coordenadas, usamos la query nativa con Haversine
        if (lat != null && lon != null) {
            List<Object[]> rows = courtRepository.findNearbyCourtsRaw(
                    lat, lon, DEFAULT_RADIUS_KM, sport, DEFAULT_LIMIT
            );
            return rows.stream()
                    .map(row -> {
                        // La última columna de la query es distance_km
                        double distanceKm = ((Number) row[row.length - 1]).doubleValue();
                        // Reconstruimos la entidad desde el repositorio para usar el mapper tipado.
                        // row[0] es id_court (primer campo de courts.*)
                        Long courtId = ((Number) row[0]).longValue();
                        Court court = courtRepository.findById(courtId).orElseThrow();
                        return courtMapper.fromRaw(row, court, distanceKm);
                    })
                    .toList();
        }

        // Fallback: sin coordenadas devolvemos las pistas activas filtradas por deporte
        List<Court> courts = courtRepository.findActiveCourts(
                sport, PageRequest.of(0, DEFAULT_LIMIT)
        );
        return courtMapper.toNearbyCourtList(courts);
    }
}
