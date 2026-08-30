package com.example.demo.controller;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.model.AppConfig;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.Reservation;
import com.example.demo.model.ReservationStatus;
import com.example.demo.service.AppConfigService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReservationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MobileApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Get Categories
    @GetMapping("/categorias")
    public List<Category> getCategories() {
        return categoryService.getActiveCategories();
    }

    // 2. Get Featured Products
    @GetMapping("/productos/destacados")
    public List<Product> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    // 3. Get Public app configuration
    @GetMapping("/configuracion")
    public AppConfig getAppConfig() {
        return appConfigService.getAppConfig();
    }

    // 4. Paginated Catalog products list with filter & sort options
    @GetMapping("/productos")
    public Page<Product> getProducts(
            @RequestParam(value = "categoria", required = false) String categoryId,
            @RequestParam(value = "orden", defaultValue = "relevante") String sortOption,
            @RequestParam(value = "buscar", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Sort sort = Sort.by("name").ascending();
        if ("precio_menor".equals(sortOption)) {
            sort = Sort.by("price").ascending();
        } else if ("precio_mayor".equals(sortOption)) {
            sort = Sort.by("price").descending();
        } else if ("mas_recientes".equals(sortOption)) {
            sort = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        if (query != null && !query.trim().isEmpty()) {
            // Replicate Catalog search query logic safely
            org.springframework.data.mongodb.core.query.Query searchQ = new org.springframework.data.mongodb.core.query.Query();
            String regex = ".*" + query.trim() + ".*";
            org.springframework.data.mongodb.core.query.Criteria crit = new org.springframework.data.mongodb.core.query.Criteria().andOperator(
                org.springframework.data.mongodb.core.query.Criteria.where("active").is(true),
                new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                    org.springframework.data.mongodb.core.query.Criteria.where("name").regex(regex, "i"),
                    org.springframework.data.mongodb.core.query.Criteria.where("brand").regex(regex, "i"),
                    org.springframework.data.mongodb.core.query.Criteria.where("activeIngredient").regex(regex, "i")
                )
            );
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                crit = new org.springframework.data.mongodb.core.query.Criteria().andOperator(
                    org.springframework.data.mongodb.core.query.Criteria.where("active").is(true),
                    org.springframework.data.mongodb.core.query.Criteria.where("categoryId").is(categoryId),
                    new org.springframework.data.mongodb.core.query.Criteria().orOperator(
                        org.springframework.data.mongodb.core.query.Criteria.where("name").regex(regex, "i"),
                        org.springframework.data.mongodb.core.query.Criteria.where("brand").regex(regex, "i"),
                        org.springframework.data.mongodb.core.query.Criteria.where("activeIngredient").regex(regex, "i")
                    )
                );
            }
            searchQ.addCriteria(crit).with(pageable);
            long total = productService.getAllProducts().size(); // approximate fallback count
            List<Product> list = productService.getAllProducts(); // simple search fallback context logic
            // Return query subset
            return new org.springframework.data.domain.PageImpl<>(
                list.stream().filter(p -> p.isActive() && 
                    (categoryId == null || categoryId.isEmpty() || p.getCategoryId().equals(categoryId)) &&
                    (p.getName().toLowerCase().contains(query.toLowerCase()) || 
                     p.getBrand().toLowerCase().contains(query.toLowerCase()) || 
                     p.getActiveIngredient().toLowerCase().contains(query.toLowerCase()))
                ).skip((long) page * size).limit(size).toList(),
                pageable,
                list.stream().filter(p -> p.isActive() && 
                    (categoryId == null || categoryId.isEmpty() || p.getCategoryId().equals(categoryId)) &&
                    (p.getName().toLowerCase().contains(query.toLowerCase()) || 
                     p.getBrand().toLowerCase().contains(query.toLowerCase()) || 
                     p.getActiveIngredient().toLowerCase().contains(query.toLowerCase()))
                ).count()
            );
        } else if (categoryId != null && !categoryId.trim().isEmpty()) {
            return productService.getActiveProductsByCategory(categoryId, pageable);
        } else {
            return productService.getActiveProducts(pageable);
        }
    }

    // 5. Product details lookup
    @GetMapping("/productos/{id}")
    public Product getProductDetails(@PathVariable("id") String id) {
        return productService.getProductById(id);
    }

    // 6. Create Reservation (Atomic stock rule deduction)
    @PostMapping("/apartados/crear")
    public ResponseEntity<?> createReservation(@RequestBody ReservationDTO dto) {
        try {
            Reservation reservation = reservationService.createReservation(
                    dto.getCustomerName(),
                    dto.getCustomerPhone(),
                    dto.getCustomerEmail(),
                    dto.getNotes(),
                    dto.getProductId(),
                    dto.getQuantity()
            );
            return ResponseEntity.ok(reservation);
        } catch (Exception ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 7. Search Reservation by Code and Phone
    @GetMapping("/apartados/buscar")
    public ResponseEntity<?> searchReservation(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam("phone") String phone) {
        try {
            if (code != null && !code.trim().isEmpty()) {
                Reservation res = reservationService.getReservationByCodeAndPhone(code, phone);
                return ResponseEntity.ok(List.of(res));
            } else {
                List<Reservation> list = reservationService.getReservationsByPhone(phone);
                return ResponseEntity.ok(list);
            }
        } catch (Exception ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 8. Admin login validation REST endpoint
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> creds) {
        String username = creds.get("username");
        String password = creds.get("password");
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                boolean isAdmin = userDetails.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("username", username);
                    result.put("role", "ADMIN");
                    return ResponseEntity.ok(result);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas o sin permisos de administrador."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no encontrado."));
        }
    }

    // 9. Admin reservations dashboard metrics REST
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("countPending", reservationService.getReservationsCountByStatus(ReservationStatus.PENDIENTE));
        metrics.put("countConfirmed", reservationService.getReservationsCountByStatus(ReservationStatus.CONFIRMADO));
        metrics.put("countPrepared", reservationService.getReservationsCountByStatus(ReservationStatus.PREPARADO));
        metrics.put("countCancelled", reservationService.getReservationsCountByStatus(ReservationStatus.CANCELADO));
        metrics.put("countDeliveredToday", reservationService.getReservationsCountDeliveredToday());
        return ResponseEntity.ok(metrics);
    }

    // 10. Admin search, filter and order list of reservations REST
    @GetMapping("/admin/apartados")
    public Page<Reservation> getAdminReservations(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) ReservationStatus status,
            @RequestParam(value = "dateFilter", required = false) String dateFilter,
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {

        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("oldest".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "createdAt");
        } else if ("highestTotal".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "total");
        } else if ("lowestTotal".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "total");
        }

        return reservationService.searchAndFilterReservations(search, status, dateFilter, PageRequest.of(page, size, sortObj));
    }

    // 11. Admin reservation transition REST trigger endpoint
    @PostMapping("/admin/apartados/estado/{id}")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable("id") String id,
            @RequestParam("status") ReservationStatus status,
            @RequestParam(value = "cancelNotes", required = false) String cancelNotes) {
        try {
            Reservation updated;
            if (status == ReservationStatus.CANCELADO) {
                updated = reservationService.cancelReservationWithNotes(id, cancelNotes);
            } else {
                updated = reservationService.updateStatus(id, status);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
