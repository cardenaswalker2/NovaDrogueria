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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class ConcurrencyTest {

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
    void testConcurrentStockOne() throws InterruptedException {
        // Set stock = 1
        testProduct.setStock(1);
        productRepository.save(testProduct);

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final String name = "User " + i;
            executor.submit(() -> {
                try {
                    latch.await(); // Wait for sync start signal
                    reservationService.createReservation(name, "300111111" + name.hashCode(), "test@t.com", "", testProduct.getId(), 1);
                    successCount.incrementAndGet();
                } catch (OutOfStockException e) {
                    failureCount.incrementAndGet();
                } catch (org.springframework.dao.DataIntegrityViolationException | org.springframework.transaction.TransactionSystemException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Start threads simultaneously
        doneLatch.await(); // Wait for all threads to complete
        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(0, product.getStock());

        long count = reservationRepository.count();
        assertEquals(1, count);
    }

    @Test
    void testConcurrentStockFiveTenRequests() throws InterruptedException {
        // Set stock = 5
        testProduct.setStock(5);
        productRepository.save(testProduct);

        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final String name = "User " + i;
            executor.submit(() -> {
                try {
                    latch.await();
                    reservationService.createReservation(name, "300111111" + name.hashCode(), "test@t.com", "", testProduct.getId(), 1);
                    successCount.incrementAndGet();
                } catch (OutOfStockException e) {
                    System.out.println("OutOfStockException: " + e.getMessage());
                    failureCount.incrementAndGet();
                } catch (org.springframework.dao.DataIntegrityViolationException | org.springframework.transaction.TransactionSystemException e) {
                    System.out.println("Write conflict / Transaction exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Unexpected Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(5, successCount.get());
        assertEquals(5, failureCount.get());

        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(0, product.getStock());

        long count = reservationRepository.count();
        assertEquals(5, count);
    }

    @Test
    void testConcurrentCancellation() throws InterruptedException {
        testProduct.setStock(5);
        productRepository.save(testProduct);

        // Reserve 1 item (stock goes 5 -> 4)
        Reservation res = reservationService.createReservation("Client X", "3001234567", "x@x.com", "", testProduct.getId(), 1);
        
        Product pAfterRes = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(4, pAfterRes.getStock());

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    reservationService.cancelReservation(res.getId());
                    successCount.incrementAndGet();
                } catch (BusinessRuleException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        Reservation updatedRes = reservationRepository.findById(res.getId()).orElseThrow();
        assertEquals(ReservationStatus.CANCELADO, updatedRes.getStatus());

        Product pFinal = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(5, pFinal.getStock()); // strictly +1, never +2
    }

    @Test
    void testConcurrentStatusTransition() throws InterruptedException {
        testProduct.setStock(5);
        productRepository.save(testProduct);

        // Reserve 1 item (default status: PENDIENTE)
        Reservation res = reservationService.createReservation("Client Y", "3001234567", "y@y.com", "", testProduct.getId(), 1);
        
        // Transition to CONFIRMADO
        res = reservationService.updateStatus(res.getId(), ReservationStatus.CONFIRMADO);
        assertEquals(ReservationStatus.CONFIRMADO, res.getStatus());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        final String resId = res.getId();

        // Thread A: CONFIRMADO -> PREPARADO
        executor.submit(() -> {
            try {
                latch.await();
                reservationService.updateStatus(resId, ReservationStatus.PREPARADO);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        // Thread B: CONFIRMADO -> CANCELADO
        executor.submit(() -> {
            try {
                latch.await();
                reservationService.updateStatus(resId, ReservationStatus.CANCELADO);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());
    }
}
