package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySlug(String slug);
    
    // Public queries
    Page<Product> findByActive(boolean active, Pageable pageable);
    Page<Product> findByActiveAndCategoryId(boolean active, String categoryId, Pageable pageable);
    
    // Featured products
    List<Product> findByActiveAndFeatured(boolean active, boolean featured);
    
    // Admin checking low stock/out of stock
    List<Product> findByStockLessThanEqual(int maxStock);
    List<Product> findByStock(int stock);
    
    // count operations
    long countByActive(boolean active);
    long countByStockLessThanEqual(int maxStock);
    long countByStock(int stock);
}
