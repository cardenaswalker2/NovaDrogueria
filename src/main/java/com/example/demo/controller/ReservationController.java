package com.example.demo.controller;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.model.Product;
import com.example.demo.model.Reservation;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/apartados")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private com.example.demo.util.ColombianCurrencyFormatter currencyFormatter;

    @ModelAttribute("currencyFormatter")
    public com.example.demo.util.ColombianCurrencyFormatter getCurrencyFormatter() {
        return currencyFormatter;
    }

    @GetMapping("/crear")
    public String showCreateForm(@RequestParam("productId") String productId, Model model) {
        Product product = productService.getProductById(productId);
        
        ReservationDTO dto = new ReservationDTO();
        dto.setProductId(productId);
        dto.setQuantity(1);

        model.addAttribute("product", product);
        model.addAttribute("reservationDTO", dto);
        return "public/reservation-create";
    }

    @PostMapping("/crear")
    public String processReservation(
            @Valid @ModelAttribute("reservationDTO") ReservationDTO dto,
            BindingResult result,
            Model model) {

        Product product = productService.getProductById(dto.getProductId());

        if (result.hasErrors()) {
            model.addAttribute("product", product);
            return "public/reservation-create";
        }

        try {
            Reservation reservation = reservationService.createReservation(
                    dto.getCustomerName(),
                    dto.getCustomerPhone(),
                    dto.getCustomerEmail(),
                    dto.getNotes(),
                    dto.getProductId(),
                    dto.getQuantity()
            );
            return "redirect:/apartados/confirmacion/" + reservation.getCode();
        } catch (Exception ex) {
            model.addAttribute("product", product);
            model.addAttribute("errorMessage", ex.getMessage());
            return "public/reservation-create";
        }
    }

    @GetMapping("/confirmacion/{code}")
    public String showConfirmation(@PathVariable("code") String code, Model model) {
        Reservation reservation = reservationService.getReservationByCode(code);
        model.addAttribute("reservation", reservation);
        return "public/reservation-confirm";
    }

    @GetMapping("/buscar")
    public String showSearchForm(Model model) {
        return "public/reservation-search";
    }

    @PostMapping("/buscar")
    public String searchReservation(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "searchType", defaultValue = "code") String searchType,
            Model model) {
        
        model.addAttribute("searchType", searchType);
        model.addAttribute("code", code);
        model.addAttribute("phone", phone);

        if ("phone".equals(searchType)) {
            if (phone == null || phone.trim().isEmpty()) {
                model.addAttribute("errorMessage", "Ingresa tu número de celular.");
                return "public/reservation-search";
            }
            String norm = reservationService.normalizePhone(phone);
            if (norm.length() < 7) {
                model.addAttribute("errorMessage", "Ingresa un número de celular válido.");
                return "public/reservation-search";
            }

            java.util.List<Reservation> list = reservationService.getReservationsByPhone(phone);
            if (list.isEmpty()) {
                model.addAttribute("noResults", true);
                return "public/reservation-search";
            }
            if (list.size() == 1) {
                model.addAttribute("reservation", list.get(0));
                return "public/reservation-search-result";
            }
            model.addAttribute("reservations", list);
            return "public/reservation-search-multiple";
        } else {
            // Code + Phone lookup
            if (code == null || code.trim().isEmpty()) {
                model.addAttribute("errorMessage", "Ingresa el código de tu apartado.");
                return "public/reservation-search";
            }
            if (!code.trim().toUpperCase().matches("^NOVA-[A-Z0-9]{6}$")) {
                model.addAttribute("errorMessage", "El código debe tener un formato como NOVA-XXXXXX.");
                return "public/reservation-search";
            }
            if (phone == null || phone.trim().isEmpty()) {
                model.addAttribute("errorMessage", "Ingresa tu número de celular.");
                return "public/reservation-search";
            }

            try {
                Reservation reservation = reservationService.getReservationByCodeAndPhone(code, phone);
                model.addAttribute("reservation", reservation);
                return "public/reservation-search-result";
            } catch (Exception ex) {
                model.addAttribute("errorMessage", ex.getMessage());
                return "public/reservation-search";
            }
        }
    }
}
