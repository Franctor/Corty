package com.corty.backend.repository;

import com.corty.backend.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String> {
    List<Province> findAllByOrderByLabelAsc();
}