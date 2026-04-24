package com.corty.backend.mapper;

import com.corty.backend.dto.NearbyCourtResponse;
import com.corty.backend.model.Court;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourtMapper {

    // Mapeo desde entidad Court (usado en el fallback sin coordenadas)
    @Mapping(target = "id", source = "idCourt")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "sport", source = "sport.name")
    @Mapping(target = "surface", source = "surface.name")
    @Mapping(target = "coverType", expression = "java(court.isCovered() ? \"indoor\" : \"outdoor\")")
    @Mapping(target = "covered", source = "covered")
    @Mapping(target = "lighting", source = "lighting")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "clubCity", source = "club.city.label")
    @Mapping(target = "distance", constant = "0.0")
    NearbyCourtResponse toNearbyCourtResponse(Court court);

    List<NearbyCourtResponse> toNearbyCourtList(List<Court> courts);

    // Mapeo manual desde Object[] (resultado de la query nativa con distancia calculada).
    // Orden de columnas según la query en CourtRepository:
    // [0..n] columnas de courts.*, luego club_name, distance_km
    default NearbyCourtResponse fromRaw(Object[] row, Court court, double distanceKm) {
        return NearbyCourtResponse.builder()
                .id(court.getIdCourt())
                .name(court.getName())
                .clubName(court.getClub().getName())
                .clubCity(court.getClub().getCity() != null ? court.getClub().getCity().getLabel() : null)
                .sport(court.getSport().getName())
                .surface(court.getSurface() != null ? court.getSurface().getName() : null)
                .pricePerHour(court.getPricePerHour())
                .distance(distanceKm)
                .coverType(court.isCovered() ? "indoor" : "outdoor")
                .covered(court.isCovered())
                .lighting(court.isLighting())
                .imageUrl(court.getImageUrl())
                .build();
    }
}
