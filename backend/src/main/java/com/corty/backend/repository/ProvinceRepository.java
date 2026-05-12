package com.corty.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String> {

    List<Province> findAllByOrderByLabelAsc();
}
