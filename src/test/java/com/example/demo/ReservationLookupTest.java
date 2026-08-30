package com.example.demo;

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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ReservationLookupTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Medicamentos");
        category.setSlug("medicamentos");
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        product = new Product();
        product.setName("Acetaminofen 500mg");
        product.setSlug("acetaminofen-500mg");
        product.setCategoryId(category.getId());
        product.setBrand("Genfar");
        product.setPresentation("Caja");
        product.setDescription("Desc");
        product.setPrice(new BigDecimal("8500.00"));
        product.setStock(10);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
    }

    @Test
    void testSearchByCodeAndPhone() {
        Reservation res = reservationService.createReservation("Diego", "3005722844", "diego@mail.com", "Notes", product.getId(), 2);
        assertNotNull(res);

        // Positive search
        Reservation found = reservationService.getReservationByCodeAndPhone(res.getCode(), "3005722844");
        assertNotNull(found);
        assertEquals(res.getCode(), found.getCode());

        // Negative search (invalid phone)
        assertThrows(Exception.class, () -> {
            reservationService.getReservationByCodeAndPhone(res.getCode(), "3111111111");
        });
    }

    @Test
    void testSearchByPhoneNormalization() {
        // Safe input variants
        Reservation res = reservationService.createReservation("Diego", "3005722844", "diego@mail.com", "Notes", product.getId(), 1);
        assertNotNull(res);

        // Normalize phone calls
        String norm1 = reservationService.normalizePhone("300 572 2844");
        String norm2 = reservationService.normalizePhone("+57 300-572-2844");
        String norm3 = reservationService.normalizePhone("3005722844");

        assertEquals("3005722844", norm1);
        assertEquals("3005722844", norm2);
        assertEquals("3005722844", norm3);

        List<Reservation> found = reservationService.getReservationsByPhone("+57 300-572-2844");
        assertFalse(found.isEmpty());
        assertEquals(res.getCode(), found.get(0).getCode());
    }

    @Test
    void testMultipleReservationsSorting() throws InterruptedException {
        // First reservation
        Reservation res1 = reservationService.createReservation("Diego", "3005722844", "diego@mail.com", "Notes", product.getId(), 1);
        Thread.sleep(100);
        // Second reservation
        Reservation res2 = reservationService.createReservation("Diego", "3005722844", "diego@mail.com", "Notes", product.getId(), 1);

        List<Reservation> list = reservationService.getReservationsByPhone("3005722844");
        assertEquals(2, list.size());
        
        // Assert sorting: most recent first (res2 first)
        assertEquals(res2.getCode(), list.get(0).getCode());
        assertEquals(res1.getCode(), list.get(1).getCode());
    }

    @Test
    void testPrivacySanitization() {
        Reservation res = reservationService.createReservation("Diego", "3005722844", "diego@mail.com", "Notes", product.getId(), 1);
        
        List<Reservation> list = reservationService.getReservationsByPhone("3005722844");
        assertFalse(list.isEmpty());
        
        // Assert email is masked/sanitized
        String email = list.get(0).getCustomerEmail();
        assertTrue(email.contains("****"));
        assertFalse(email.equals("diego@mail.com"));
    }
}
