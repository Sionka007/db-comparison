package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.Brand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandService {
    Brand save(Brand brand);
    Optional<Brand> findById(UUID id);
    List<Brand> findAll();
    void deleteById(UUID id);
}
