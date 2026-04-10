package com.corty.backend.mapper;

import com.corty.backend.dto.CourtAdminResponse;
import com.corty.backend.dto.CourtRequest;
import com.corty.backend.model.Court;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourtAdminMapper {

    @Mapping(target = "id", source = "idCourt")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "sportName", source = "sport.name")
    @Mapping(target = "surfaceName", source = "surface.name")
    CourtAdminResponse toResponse(Court court);

    List<CourtAdminResponse> toResponseList(List<Court> courts);

    @Mapping(target = "idCourt", ignore = true)
    @Mapping(target = "club", ignore = true)
    @Mapping(target = "sport", ignore = true)
    @Mapping(target = "surface", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    Court toEntity(CourtRequest request);

    @Mapping(target = "idCourt", ignore = true)
    @Mapping(target = "club", ignore = true)
    @Mapping(target = "sport", ignore = true)
    @Mapping(target = "surface", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    void updateEntity(CourtRequest request, @MappingTarget Court court);
}
