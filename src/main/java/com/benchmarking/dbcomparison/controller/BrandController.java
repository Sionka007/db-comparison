package com.benchmarking.dbcomparison.controller;

import com.benchmarking.dbcomparison.model.Brand;
import com.benchmarking.dbcomparison.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<Brand> getAll() {
        return brandService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getById(@PathVariable UUID id) {
        Optional<Brand> brand = brandService.findById(id);
        return brand.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Brand create(@RequestBody Brand brand) {
        return brandService.save(brand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> update(@PathVariable UUID id, @RequestBody Brand brand) {
        if (!brandService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        brand.setId(id);
        return ResponseEntity.ok(brandService.save(brand));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!brandService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        brandService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
