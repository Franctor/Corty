package com.corty.backend.mapper;

import com.corty.backend.dto.SurfaceRequest;
import com.corty.backend.dto.SurfaceResponse;
import com.corty.backend.model.Surface;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SurfaceMapper {

    @Mapping(target = "id", source = "idSurface")
    SurfaceResponse toResponse(Surface surface);

    List<SurfaceResponse> toResponseList(List<Surface> surfaces);

    @Mapping(target = "idSurface", ignore = true)
    @Mapping(target = "courts", ignore = true)
    Surface toEntity(SurfaceRequest request);

    @Mapping(target = "idSurface", ignore = true)
    @Mapping(target = "courts", ignore = true)
    void updateEntity(SurfaceRequest request, @MappingTarget Surface surface);
}
