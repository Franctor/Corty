package com.corty.backend.mapper;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.dto.SportRequest;
import com.corty.backend.dto.SportResponse;
import com.corty.backend.model.Sport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SportMapper {

    @Mapping(target = "id", source = "idSport")
    SportFilterResponse toFilterResponse(Sport sport);

    List<SportFilterResponse> toFilterResponseList(List<Sport> sports);

    @Mapping(target = "id", source = "idSport")
    SportResponse toResponse(Sport sport);

    List<SportResponse> toResponseList(List<Sport> sports);

    @Mapping(target = "idSport", ignore = true)
    @Mapping(target = "availablePositions", ignore = true)
    @Mapping(target = "playersProfiles", ignore = true)
    @Mapping(target = "courts", ignore = true)
    Sport toEntity(SportRequest request);

    @Mapping(target = "idSport", ignore = true)
    @Mapping(target = "availablePositions", ignore = true)
    @Mapping(target = "playersProfiles", ignore = true)
    @Mapping(target = "courts", ignore = true)
    void updateEntity(SportRequest request, @MappingTarget Sport sport);
}
