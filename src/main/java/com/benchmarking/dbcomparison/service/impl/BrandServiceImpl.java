package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.Brand;
import com.benchmarking.dbcomparison.repository.BrandRepository;
import com.benchmarking.dbcomparison.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    public Optional<Brand> findById(UUID id) {
        return brandRepository.findById(id);
    }

    @Override
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        brandRepository.deleteById(id);
    }
}
