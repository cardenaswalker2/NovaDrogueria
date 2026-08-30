package com.example.demo.service;

import com.example.demo.model.Reservation;
import com.example.demo.model.ReservationItem;
import com.example.demo.model.ReservationStatus;
import com.example.demo.model.Product;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.OutOfStockException;
import com.example.demo.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.model.ReservationStatusHistory;
import java.util.Random;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    public Page<Reservation> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        return reservationRepository.findByStatus(status, pageable);
    }

    public Reservation getReservationById(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con el ID: " + id));
    }

    public Reservation getReservationByCode(String code) {
        return reservationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con el código: " + code));
    }

    public String normalizePhone(String phone) {
        if (phone == null) return "";
        // Extract only digits
        String digits = phone.replaceAll("[^0-9]", "");
        // Remove Colombia country prefix 57 if present at the start of a 12-digit number (573XXXXXXXXX)
        if (digits.length() == 12 && digits.startsWith("57")) {
            return digits.substring(2);
        }
        return digits;
    }

    public Reservation getReservationByCodeAndPhone(String code, String phone) {
        String normalizedCode = code.trim().toUpperCase();
        String normalizedPhone = normalizePhone(phone);
        
        // Find by code
        Reservation reservation = getReservationByCode(normalizedCode);
        String resPhone = normalizePhone(reservation.getCustomerPhone());
        
        if (resPhone.equals(normalizedPhone)) {
            return reservation;
        }
        throw new BusinessRuleException("Apartado no encontrado. Verifique el código y teléfono ingresado.");
    }

    public List<Reservation> getReservationsByPhone(String phone) {
        String targetPhone = normalizePhone(phone);
        if (targetPhone.isEmpty()) {
            return new ArrayList<>();
        }
        
        // We fetch all reservations and compare normalized values to match historical formats safely
        List<Reservation> all = reservationRepository.findAll();
        List<Reservation> matches = new ArrayList<>();
        for (Reservation res : all) {
            if (normalizePhone(res.getCustomerPhone()).equals(targetPhone)) {
                // Sanitize sensitive user properties to prevent leak
                res.setCustomerEmail(sanitizeEmail(res.getCustomerEmail()));
                matches.add(res);
            }
        }
        
        // Sort descending by createdAt
        matches.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        
        return matches;
    }

    private String sanitizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "";
        int index = email.indexOf('@');
        if (index <= 1) return "***@novadrogueria.com";
        return email.charAt(0) + "****" + email.substring(index - 1);
    }

    public Reservation createReservation(String customerName, String customerPhone, String customerEmail, String notes, String productId, int quantity) {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                // Call the proxy method to get a new transaction context on each attempt
                return selfProxy().createReservationInternal(customerName, customerPhone, customerEmail, notes, productId, quantity);
            } catch (org.springframework.dao.DataIntegrityViolationException | org.springframework.transaction.TransactionSystemException e) {
                String msg = e.getMessage();
                if (msg != null && (msg.contains("WriteConflict") || msg.contains("NoSuchTransaction") || msg.contains("aborted"))) {
                    try { Thread.sleep(50 + new Random().nextInt(50)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                throw e;
            }
        }
        throw new BusinessRuleException("No se pudo completar la reserva debido a conflictos de concurrencia.");
    }

    @Autowired
    private org.springframework.context.ApplicationContext context;

    private ReservationService selfProxy() {
        return context.getBean(ReservationService.class);
    }

    @Transactional
    public Reservation createReservationInternal(String customerName, String customerPhone, String customerEmail, String notes, String productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("La cantidad a apartar debe ser mayor a 0.");
        }

        // 1. Perform atomic stock deduction & active check
        Query stockQuery = new Query(Criteria.where("id").is(productId)
                .and("stock").gte(quantity)
                .and("active").is(true));
        Update stockUpdate = new Update().inc("stock", -quantity);
        Product product = mongoTemplate.findAndModify(stockQuery, stockUpdate, Product.class);

        if (product == null) {
            throw new OutOfStockException("El producto solicitado no tiene stock suficiente o no se encuentra activo.");
        }

        // 2. Generate unique human-readable reservation code with collision protection
        String code;
        synchronized (this) {
            code = generateUniqueCode();
        }

        // 3. Create reservation snapshot
        Reservation reservation = new Reservation();
        reservation.setCode(code);
        reservation.setCustomerName(customerName);
        reservation.setCustomerPhone(customerPhone);
        reservation.setCustomerEmail(customerEmail);
        reservation.setNotes(notes);
        reservation.setStatus(ReservationStatus.PENDIENTE);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        ReservationItem item = new ReservationItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantity
        );
        reservation.getItems().add(item);
        
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        reservation.setTotal(total);

        // Set initial status history
        reservation.getStatusHistory().add(new ReservationStatusHistory(ReservationStatus.PENDIENTE, LocalDateTime.now(), "Apartado creado por el cliente"));

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(String id, ReservationStatus targetStatus) {
        Reservation reservation = getReservationById(id);
        ReservationStatus currentStatus = reservation.getStatus();

        if (currentStatus == targetStatus) {
            if (currentStatus == ReservationStatus.CANCELADO || currentStatus == ReservationStatus.ENTREGADO) {
                throw new BusinessRuleException("El apartado ya se encuentra en el estado final: " + currentStatus.getDisplayName());
            }
            return reservation; // Already in target state, no change
        }

        // Transition rule checks
        boolean valid = false;
        if (currentStatus == ReservationStatus.PENDIENTE) {
            valid = (targetStatus == ReservationStatus.CONFIRMADO || targetStatus == ReservationStatus.CANCELADO);
        } else if (currentStatus == ReservationStatus.CONFIRMADO) {
            valid = (targetStatus == ReservationStatus.PREPARADO || targetStatus == ReservationStatus.CANCELADO);
        } else if (currentStatus == ReservationStatus.PREPARADO) {
            valid = (targetStatus == ReservationStatus.ENTREGADO || targetStatus == ReservationStatus.CANCELADO);
        }

        if (!valid) {
            throw new BusinessRuleException("Transición de estado no válida desde: " + currentStatus + " hacia " + targetStatus);
        }

        // If transitioning to CANCELADO, restore stock
        if (targetStatus == ReservationStatus.CANCELADO) {
            for (ReservationItem item : reservation.getItems()) {
                Query productQuery = new Query(Criteria.where("id").is(item.getProductId()));
                Update productUpdate = new Update().inc("stock", item.getQuantity());
                mongoTemplate.updateFirst(productQuery, productUpdate, Product.class);
            }
        }

        // Add history entry
        if (reservation.getStatusHistory() == null) {
            reservation.setStatusHistory(new ArrayList<>());
        }
        reservation.getStatusHistory().add(new ReservationStatusHistory(targetStatus, LocalDateTime.now(), "Estado actualizado por administrador"));

        reservation.setStatus(targetStatus);
        
        // Push status and statusHistory updates atomically
        Query updateQuery = new Query(Criteria.where("id").is(id).and("status").is(currentStatus));
        Update updateDoc = new Update()
                .set("status", targetStatus)
                .set("updatedAt", LocalDateTime.now())
                .set("statusHistory", reservation.getStatusHistory());
        
        long modifiedCount = mongoTemplate.updateFirst(updateQuery, updateDoc, Reservation.class).getModifiedCount();
        if (modifiedCount == 0) {
            throw new BusinessRuleException("No se pudo actualizar el estado de la reserva. Es posible que haya sido modificada por otro usuario.");
        }

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(String id) {
        return updateStatus(id, ReservationStatus.CANCELADO);
    }

    private String generateUniqueCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed easily confused chars O, I, 1, 0
        Random rnd = new Random();
        String code;
        boolean isUnique;
        int limit = 0;
        do {
            StringBuilder sb = new StringBuilder("NOVA-");
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            code = sb.toString();
            isUnique = !mongoTemplate.exists(new Query(Criteria.where("code").is(code)), Reservation.class);
            limit++;
            if (limit > 50) {
                // emergency fallback
                code = "NOVA-" + System.currentTimeMillis() % 1000000;
                break;
            }
        } while (!isUnique);
        return code;
    }

    public long getPendingCount() {
        return reservationRepository.countByStatus(ReservationStatus.PENDIENTE);
    }

    public long getReservationsCountByStatus(ReservationStatus status) {
        return reservationRepository.countByStatus(status);
    }

    public long getReservationsCountToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return reservationRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    public long getReservationsCountDeliveredToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Query query = new Query(Criteria.where("status").is(ReservationStatus.ENTREGADO)
                .and("updatedAt").gte(startOfDay).lte(endOfDay));
        return mongoTemplate.count(query, Reservation.class);
    }

    @Transactional
    public Reservation cancelReservationWithNotes(String id, String notes) {
        Reservation reservation = getReservationById(id);
        ReservationStatus currentStatus = reservation.getStatus();
        
        if (currentStatus == ReservationStatus.CANCELADO || currentStatus == ReservationStatus.ENTREGADO) {
            throw new BusinessRuleException("No se puede cancelar un apartado en estado: " + currentStatus.getDisplayName());
        }

        // Restore stock
        for (ReservationItem item : reservation.getItems()) {
            Query productQuery = new Query(Criteria.where("id").is(item.getProductId()));
            Update productUpdate = new Update().inc("stock", item.getQuantity());
            mongoTemplate.updateFirst(productQuery, productUpdate, Product.class);
        }

        // Add history entry with custom notes
        String cancelNotes = (notes != null && !notes.trim().isEmpty()) ? notes : "Cancelado por el administrador";
        if (reservation.getStatusHistory() == null) {
            reservation.setStatusHistory(new ArrayList<>());
        }
        reservation.getStatusHistory().add(new ReservationStatusHistory(ReservationStatus.CANCELADO, LocalDateTime.now(), cancelNotes));
        reservation.setStatus(ReservationStatus.CANCELADO);

        Query updateQuery = new Query(Criteria.where("id").is(id).and("status").is(currentStatus));
        Update updateDoc = new Update()
                .set("status", ReservationStatus.CANCELADO)
                .set("updatedAt", LocalDateTime.now())
                .set("statusHistory", reservation.getStatusHistory());

        long modifiedCount = mongoTemplate.updateFirst(updateQuery, updateDoc, Reservation.class).getModifiedCount();
        if (modifiedCount == 0) {
            throw new BusinessRuleException("No se pudo cancelar el apartado. Es posible que haya sido modificado por otro usuario.");
        }

        return reservation;
    }

    public Page<Reservation> searchAndFilterReservations(String search, ReservationStatus status, String dateFilter, Pageable pageable) {
        Query query = new Query();

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("code").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerName").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerPhone").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerEmail").regex(".*" + s + ".*", "i")
            );
            query.addCriteria(searchCriteria);
        }

        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            if (dateFilter.equals("today")) {
                query.addCriteria(Criteria.where("createdAt").gte(LocalDateTime.of(LocalDate.now(), LocalTime.MIN)));
            } else if (dateFilter.equals("yesterday")) {
                LocalDateTime startOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
                LocalDateTime endOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
                query.addCriteria(Criteria.where("createdAt").gte(startOfYesterday).lte(endOfYesterday));
            } else if (dateFilter.equals("week")) {
                query.addCriteria(Criteria.where("createdAt").gte(now.minusDays(7)));
            } else if (dateFilter.equals("month")) {
                query.addCriteria(Criteria.where("createdAt").gte(now.minusDays(30)));
            }
        }

        query.with(pageable);
        List<Reservation> list = mongoTemplate.find(query, Reservation.class);
        
        // Count total matching documents
        Query countQuery = new Query();
        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            countQuery.addCriteria(new Criteria().orOperator(
                    Criteria.where("code").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerName").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerPhone").regex(".*" + s + ".*", "i"),
                    Criteria.where("customerEmail").regex(".*" + s + ".*", "i")
            ));
        }
        if (status != null) {
            countQuery.addCriteria(Criteria.where("status").is(status));
        }
        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            if (dateFilter.equals("today")) {
                countQuery.addCriteria(Criteria.where("createdAt").gte(LocalDateTime.of(LocalDate.now(), LocalTime.MIN)));
            } else if (dateFilter.equals("yesterday")) {
                LocalDateTime startOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
                LocalDateTime endOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);
                countQuery.addCriteria(Criteria.where("createdAt").gte(startOfYesterday).lte(endOfYesterday));
            } else if (dateFilter.equals("week")) {
                countQuery.addCriteria(Criteria.where("createdAt").gte(now.minusDays(7)));
            } else if (dateFilter.equals("month")) {
                countQuery.addCriteria(Criteria.where("createdAt").gte(now.minusDays(30)));
            }
        }
        long totalCount = mongoTemplate.count(countQuery, Reservation.class);

        return new org.springframework.data.domain.PageImpl<>(list, pageable, totalCount);
    }
}
