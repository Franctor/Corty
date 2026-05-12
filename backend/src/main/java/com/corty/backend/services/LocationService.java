package com.corty.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.corty.backend.dto.CityResponse;
import com.corty.backend.dto.ProvinceResponse;
import com.corty.backend.model.City;
import com.corty.backend.repository.CityRepository;
import com.corty.backend.repository.ProvinceRepository;

import lombok.RequiredArgsConstructor;

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

    public List<CityResponse> getAllCities() {
        return cityRepository.findAllByOrderByLabelAsc()
                .stream()
                .map(c -> CityResponse.builder()
                .idCity(c.getIdCity())
                .label(c.getLabel())
                .provinceCode(c.getProvince().getCode())
                .build())
                .toList();
    }

    public CityResponse getCityById(Long cityId) {
        return cityRepository.findById(cityId)
                .map(c -> CityResponse.builder()
                .idCity(c.getIdCity())
                .label(c.getLabel())
                .provinceCode(c.getProvince().getCode())
                .build())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("City not found"));
    }

    public List<CityResponse> searchCitiesByName(String name) {
        List<City> results = cityRepository.findByLabelContainingIgnoreCaseOrderByLabelAsc(name);
        results.sort((first, second) -> {
            boolean firstExact = first.getLabel().equalsIgnoreCase(name);
            boolean secondExact = second.getLabel().equalsIgnoreCase(name);
            if (firstExact && !secondExact) {
                return -1;
            }
            if (!firstExact && secondExact) {
                return 1;
            }
            // Shorter label = more specific match (avoids "Guardia de Jaén, La" beating "Jaén")
            int lengthDiff = first.getLabel().length() - second.getLabel().length();
            if (lengthDiff != 0) {
                return lengthDiff;
            }
            return first.getLabel().compareTo(second.getLabel());
        });
        return results.stream()
                .map(c -> CityResponse.builder()
                .idCity(c.getIdCity())
                .label(c.getLabel())
                .provinceCode(c.getProvince().getCode())
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
