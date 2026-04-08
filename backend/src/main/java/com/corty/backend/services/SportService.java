package com.corty.backend.services;

import com.corty.backend.dto.SportFilterResponse;
import com.corty.backend.mapper.SportMapper;
import com.corty.backend.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SportService {

    private static final int FILTER_LIMIT = 5;

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;

    public List<SportFilterResponse> getTopSportsForFilter() {
        return sportMapper.toFilterResponseList(
                sportRepository.findTopByPopularity(PageRequest.of(0, FILTER_LIMIT))
        );
    }
}
