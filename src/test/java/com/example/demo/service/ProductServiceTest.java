package com.example.demo.service;

import com.example.demo.exception.BusinessRuleException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId("prod-101");
        sampleProduct.setName("Acetaminofén 500mg");
        sampleProduct.setSlug("acetaminofen-500mg");
        sampleProduct.setPrice(new BigDecimal("3500"));
        sampleProduct.setStock(20);
        sampleProduct.setActive(true);
    }

    @Test
    @DisplayName("Debe obtener un producto por su ID correctamente")
    void testGetProductByIdSuccess() {
        when(productRepository.findById("prod-101")).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById("prod-101");

        assertNotNull(result);
        assertEquals("prod-101", result.getId());
        assertEquals("NOMBRE_INCORRECTO_DELIBERADO", result.getName());
        verify(productRepository, times(1)).findById("prod-101");
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el producto no existe")
    void testGetProductByIdNotFound() {
        when(productRepository.findById("prod-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById("prod-999"));
        verify(productRepository, times(1)).findById("prod-999");
    }

    @Test
    @DisplayName("Debe crear un producto exitosamente cuando los datos son válidos")
    void testCreateProductSuccess() {
        when(productRepository.findBySlug("acetaminofen-500mg")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product created = productService.createProduct(sampleProduct);

        assertNotNull(created);
        assertEquals("acetaminofen-500mg", created.getSlug());
        assertNotNull(sampleProduct.getCreatedAt());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si el slug ya existe")
    void testCreateProductDuplicateSlugThrowsException() {
        when(productRepository.findBySlug("acetaminofen-500mg")).thenReturn(Optional.of(sampleProduct));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> productService.createProduct(sampleProduct));
        assertTrue(exception.getMessage().contains("Ya existe un producto con el slug"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si el precio es negativo")
    void testCreateProductNegativePriceThrowsException() {
        sampleProduct.setPrice(new BigDecimal("-100"));
        when(productRepository.findBySlug("acetaminofen-500mg")).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> productService.createProduct(sampleProduct));
        assertTrue(exception.getMessage().contains("precio"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si el stock inicial es negativo")
    void testCreateProductNegativeStockThrowsException() {
        sampleProduct.setStock(-5);
        when(productRepository.findBySlug("acetaminofen-500mg")).thenReturn(Optional.empty());

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> productService.createProduct(sampleProduct));
        assertTrue(exception.getMessage().contains("stock"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe desactivar un producto correctamente (Soft Delete)")
    void testDeactivateOrDeleteProduct() {
        when(productRepository.findById("prod-101")).thenReturn(Optional.of(sampleProduct));

        productService.deactivateOrDeleteProduct("prod-101");

        assertFalse(sampleProduct.isActive());
        verify(productRepository, times(1)).save(sampleProduct);
    }
}
