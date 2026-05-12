package com.corty.backend.repository;

import com.corty.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByCode(String code);

    List<City> findByProvince_CodeOrderByLabelAsc(String provinceCode);

    List<City> findAllByOrderByLabelAsc();

    List<City> findByLabelContainingIgnoreCaseOrderByLabelAsc(String label);

    Optional<City> findByLabelIgnoreCase(String label);
}
