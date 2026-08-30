package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActive(true, pageable);
    }

    public Page<Product> getActiveProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByActiveAndCategoryId(true, categoryId, pageable);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByActiveAndFeatured(true, true);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con la dirección slug: " + slug));
    }

    public Product createProduct(Product product) {
        if (productRepository.findBySlug(product.getSlug()).isPresent()) {
            throw new BusinessRuleException("Ya existe un producto con el slug web proporcionado.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El precio del producto debe ser mayor o igual a 0.");
        }
        if (product.getStock() < 0) {
            throw new BusinessRuleException("El stock inicial no puede ser negativo.");
        }
        
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product productDetails) {
        Product product = getProductById(id);

        if (!product.getSlug().equals(productDetails.getSlug())) {
            if (productRepository.findBySlug(productDetails.getSlug()).isPresent()) {
                throw new BusinessRuleException("Ya existe otro producto con el slug proporcionado.");
            }
            product.setSlug(productDetails.getSlug());
        }

        if (productDetails.getPrice() == null || productDetails.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("El precio debe ser mayor o igual a 0.");
        }
        if (productDetails.getStock() < 0) {
            throw new BusinessRuleException("El stock no puede ser menor a 0.");
        }

        product.setName(productDetails.getName());
        product.setCategoryId(productDetails.getCategoryId());
        product.setBrand(productDetails.getBrand());
        product.setActiveIngredient(productDetails.getActiveIngredient());
        product.setPresentation(productDetails.getPresentation());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setFeatured(productDetails.isFeatured());
        product.setActive(productDetails.isActive());
        product.setAdditionalInfo(productDetails.getAdditionalInfo());
        product.setWarnings(productDetails.getWarnings());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    public void deactivateOrDeleteProduct(String id) {
        Product product = getProductById(id);
        // Soft delete is preferred to avoid breaking historical records
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public void updateStock(String id, int newStock) {
        if (newStock < 0) {
            throw new BusinessRuleException("El inventario no puede quedar en un valor menor a 0.");
        }
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("stock", newStock).set("updatedAt", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, Product.class);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockLessThanEqual(5);
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStock(0);
    }
}
