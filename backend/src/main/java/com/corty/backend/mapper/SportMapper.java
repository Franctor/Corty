package com.corty.backend.mapper;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.model.Sport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SportMapper {

    @Mapping(target = "id", source = "idSport")
    SportFilterResponse toFilterResponse(Sport sport);

    List<SportFilterResponse> toFilterResponseList(List<Sport> sports);
}
