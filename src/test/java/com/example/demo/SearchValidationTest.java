package com.example.demo;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.ActiveProfiles("test")
class SearchValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Medicamentos");
        category.setSlug("medicamentos");
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        // Product A: Active, Name match
        Product p1 = new Product();
        p1.setName("Acetaminofen 500mg");
        p1.setSlug("acetaminofen-500mg");
        p1.setBrand("Genfar");
        p1.setActiveIngredient("Acetaminofen");
        p1.setCategoryId(category.getId());
        p1.setPrice(new BigDecimal("5.00"));
        p1.setStock(10);
        p1.setActive(true);
        p1.setCreatedAt(LocalDateTime.now());
        p1.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p1);

        // Product B: Active, Brand match
        Product p2 = new Product();
        p2.setName("Redoxon Forte");
        p2.setSlug("redoxon-forte");
        p2.setBrand("Bayer Health");
        p2.setActiveIngredient("Vitamina C");
        p2.setCategoryId(category.getId());
        p2.setPrice(new BigDecimal("12.50"));
        p2.setStock(8);
        p2.setActive(true);
        p2.setCreatedAt(LocalDateTime.now());
        p2.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p2);

        // Product C: Inactive, matches keyword
        Product p3 = new Product();
        p3.setName("Acetaminofen Jarabe");
        p3.setSlug("acetaminofen-jarabe");
        p3.setBrand("Genfar");
        p3.setActiveIngredient("Acetaminofen");
        p3.setCategoryId(category.getId());
        p3.setPrice(new BigDecimal("8.00"));
        p3.setStock(5);
        p3.setActive(false);
        p3.setCreatedAt(LocalDateTime.now());
        p3.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p3);
    }

    @Test
    void testSearchByNameBrandAndActiveIngredient() {
        // Query q=ace (matches Acetaminofen 500mg via name/activeIngredient, ignores inactive Acetaminofen Jarabe)
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                "/api/productos/buscar?q=ace",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );
        assertEquals(200, response.getStatusCode().value());
        List<Product> products = response.getBody();
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Acetaminofen 500mg", products.get(0).getName());

        // Query q=baye (matches Redoxon Forte via brand "Bayer Health")
        response = restTemplate.exchange(
                "/api/productos/buscar?q=baye",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );
        products = response.getBody();
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("Redoxon Forte", products.get(0).getName());
    }

    @Test
    void testSearchShortQueryReturnsEmptyList() {
        ResponseEntity<List<Product>> response = restTemplate.exchange(
                "/api/productos/buscar?q=",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {}
        );
        List<Product> products = response.getBody();
        assertNotNull(products);
        assertTrue(products.isEmpty());
    }
}
