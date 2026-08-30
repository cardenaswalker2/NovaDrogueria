package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByActive(true);
    }

    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + id));
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el slug: " + slug));
    }

    public Category createCategory(Category category) {
        // Validate uniqueness
        if (categoryRepository.findBySlug(category.getSlug()).isPresent()) {
            throw new BusinessRuleException("Ya existe una categoría con la dirección web (slug) proporcionada.");
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category updateCategory(String id, Category categoryDetails) {
        Category category = getCategoryById(id);
        
        // Validate slug uniqueness if changed
        if (!category.getSlug().equals(categoryDetails.getSlug())) {
            if (categoryRepository.findBySlug(categoryDetails.getSlug()).isPresent()) {
                throw new BusinessRuleException("Ya existe otra categoría con el slug proporcionado.");
            }
            category.setSlug(categoryDetails.getSlug());
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setActive(categoryDetails.isActive());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public void deleteOrDeactivateCategory(String id) {
        Category category = getCategoryById(id);
        
        // Rule: If category has products, do not allow delete. Deactivate instead or throw error
        long productCount = productRepository.countByActive(true); // Rough validation, check matching category
        // Better validation: Query any active products referencing this Category ID
        boolean hasProducts = productRepository.findByActiveAndCategoryId(true, id, PageRequest.of(0, 1)).hasContent();
        
        if (hasProducts) {
            // Soft delete / deactivate
            category.setActive(false);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);
            throw new BusinessRuleException("La categoría tiene productos asociados y no se puede eliminar físicamente. Ha sido desactivada en su lugar.");
        } else {
            // Physical delete is fine if no dependencies
            categoryRepository.delete(category);
        }
    }
}
