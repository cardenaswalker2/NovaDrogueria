package com.example.demo.repository;

import com.example.demo.model.Reservation;
import com.example.demo.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    Optional<Reservation> findByCode(String code);
    Optional<Reservation> findByCodeAndCustomerPhone(String code, String customerPhone);
    List<Reservation> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);
    
    List<Reservation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    long countByStatus(ReservationStatus status);
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
