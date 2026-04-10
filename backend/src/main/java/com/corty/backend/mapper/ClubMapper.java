package com.corty.backend.mapper;

import com.corty.backend.dto.ClubRequest;
import com.corty.backend.dto.ClubResponse;
import com.corty.backend.model.Club;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClubMapper {

    @Mapping(target = "id", source = "idClub")
    @Mapping(target = "cityName", source = "city.label")
    ClubResponse toResponse(Club club);

    List<ClubResponse> toResponseList(List<Club> clubs);

    @Mapping(target = "idClub", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Club toEntity(ClubRequest request);

    @Mapping(target = "idClub", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ClubRequest request, @MappingTarget Club club);
}
