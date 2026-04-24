package com.corty.backend.mapper;

import com.corty.backend.dto.CourtDetailResponse;
import com.corty.backend.model.Court;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourtDetailMapper {

    @Mapping(target = "id",              source = "idCourt")
    @Mapping(target = "clubName",        source = "club.name")
    @Mapping(target = "clubCity",        source = "club.city.label")
    @Mapping(target = "clubAddress",     source = "club.address")
    @Mapping(target = "clubPhone",       source = "club.phone")
    @Mapping(target = "clubEmail",       source = "club.contactEmail")
    @Mapping(target = "clubDescription", source = "club.description")
    @Mapping(target = "clubLogoUrl",     source = "club.logoUrl")
    @Mapping(target = "sportName",       source = "sport.name")
    @Mapping(target = "surfaceName",     source = "surface.name")
    CourtDetailResponse toResponse(Court court);
}
