package com.example.demo;

import com.example.demo.exception.BusinessRuleException;
import com.example.demo.exception.OutOfStockException;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.Reservation;
import com.example.demo.model.ReservationStatus;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class ReservationFlowIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Test Category");
        category.setSlug("test-category");
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setSlug("test-product");
        testProduct.setCategoryId(category.getId());
        testProduct.setBrand("Test Brand");
        testProduct.setPresentation("Box");
        testProduct.setDescription("Desc");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStock(5);
        testProduct.setActive(true);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testNormalReservationFlow() {
        // stock = 5, reserve 2
        Reservation res = reservationService.createReservation("Client A", "3001234567", "a@a.com", "notes", testProduct.getId(), 2);
        assertNotNull(res);
        assertEquals(2, res.getItems().get(0).getQuantity());
        assertEquals(ReservationStatus.PENDIENTE, res.getStatus());

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(3, updated.getStock());

        // Cancel
        reservationService.cancelReservation(res.getId());
        Product cancelledProd = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(5, cancelledProd.getStock());
    }

    @Test
    void testOutOfStockThrowsException() {
        // stock = 5, reserve 6
        assertThrows(OutOfStockException.class, () -> {
            reservationService.createReservation("Client A", "3001234567", "a@a.com", "notes", testProduct.getId(), 6);
        });

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(5, updated.getStock()); // stock stays 5
    }

    @Test
    void testDoubleCancellationIdempotency() {
        Reservation res = reservationService.createReservation("Client B", "3001234567", "b@b.com", "notes", testProduct.getId(), 2);
        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(3, updated.getStock());

        // Cancel first time
        reservationService.cancelReservation(res.getId());
        Product p1 = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(5, p1.getStock());

        // Cancel second time should throw business exception and not increment stock again
        assertThrows(BusinessRuleException.class, () -> {
            reservationService.cancelReservation(res.getId());
        });

        Product p2 = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(5, p2.getStock()); // Stays 5, not 7
    }
}
