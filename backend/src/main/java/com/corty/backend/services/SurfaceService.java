package com.corty.backend.services;

import com.corty.backend.dto.SurfaceRequest;
import com.corty.backend.dto.SurfaceResponse;
import com.corty.backend.exception.EntityInUseException;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.mapper.SurfaceMapper;
import com.corty.backend.model.Surface;
import com.corty.backend.repository.SurfaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurfaceService {

    private final SurfaceRepository surfaceRepository;
    private final SurfaceMapper surfaceMapper;

    public List<SurfaceResponse> getAll() {
        return surfaceMapper.toResponseList(surfaceRepository.findAll());
    }

    public SurfaceResponse getById(Long id) {
        return surfaceMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public SurfaceResponse create(SurfaceRequest request) {
        Surface surface = surfaceMapper.toEntity(request);
        return surfaceMapper.toResponse(surfaceRepository.save(surface));
    }

    @Transactional
    public SurfaceResponse update(Long id, SurfaceRequest request) {
        Surface surface = findOrThrow(id);
        surfaceMapper.updateEntity(request, surface);
        return surfaceMapper.toResponse(surfaceRepository.save(surface));
    }

    @Transactional
    public void delete(Long id) {
        Surface surface = findOrThrow(id);
        if (!surface.getCourts().isEmpty()) {
            throw new EntityInUseException("No se puede eliminar la superficie porque tiene pistas asociadas");
        }
        surfaceRepository.delete(surface);
    }

    private Surface findOrThrow(Long id) {
        return surfaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Superficie no encontrada"));
    }
}
