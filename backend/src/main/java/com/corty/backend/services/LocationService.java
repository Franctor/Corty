package com.corty.backend.services;

import com.corty.backend.dto.CityResponse;
import com.corty.backend.dto.ProvinceResponse;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final ProvinceRepository provinceRepository;
    private final CityRepository cityRepository;

    public List<ProvinceResponse> getAllProvinces() {
        return provinceRepository.findAllByOrderByLabelAsc()
                .stream()
                .map(p -> ProvinceResponse.builder()
                        .code(p.getCode())
                        .label(p.getLabel())
                        .build())
                .toList();
    }

    public List<CityResponse> getCitiesByProvince(String provinceCode) {
        return cityRepository.findByProvince_CodeOrderByLabelAsc(provinceCode)
                .stream()
                .map(c -> CityResponse.builder()
                        .idCity(c.getIdCity())
                        .label(c.getLabel())
                        .provinceCode(c.getProvince().getCode())
                        .build())
                .toList();
    }
}