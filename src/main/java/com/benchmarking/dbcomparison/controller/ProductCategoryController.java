package com.benchmarking.dbcomparison.controller;

import com.benchmarking.dbcomparison.model.ProductCategory;
import com.benchmarking.dbcomparison.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {

    @Autowired
    private ProductCategoryService categoryService;

    @GetMapping
    public List<ProductCategory> getAll() {
        return categoryService.findAll();
    }

    @GetMapping("/root")
    public List<ProductCategory> getRootCategories() {
        return categoryService.findRootCategories();
    }

    @GetMapping("/{id}/subcategories")
    public List<ProductCategory> getSubcategories(@PathVariable UUID id) {
        return categoryService.findSubcategories(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getById(@PathVariable UUID id) {
        Optional<ProductCategory> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductCategory create(@RequestBody ProductCategory category) {
        return categoryService.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategory> update(@PathVariable UUID id, @RequestBody ProductCategory category) {
        if (!categoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        category.setId(id);
        return ResponseEntity.ok(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!categoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
