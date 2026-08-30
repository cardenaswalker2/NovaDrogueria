package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.model.*;
import com.example.demo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private com.example.demo.util.ColombianCurrencyFormatter currencyFormatter;

    @ModelAttribute("currencyFormatter")
    public com.example.demo.util.ColombianCurrencyFormatter getCurrencyFormatter() {
        return currencyFormatter;
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalProducts = productService.getAllProducts().size();
        long activeProducts = productService.getActiveProducts(PageRequest.of(0, 1)).getTotalElements();
        long lowStockCount = productService.getLowStockProducts().size();
        long outOfStockCount = productService.getOutOfStockProducts().size();
        long pendingReservations = reservationService.getPendingCount();
        long reservationsToday = reservationService.getReservationsCountToday();

        List<Reservation> recentReservations = reservationService.getAllReservations(PageRequest.of(0, 5)).getContent();
        List<Product> lowStockList = productService.getLowStockProducts();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("pendingReservations", pendingReservations);
        model.addAttribute("reservationsToday", reservationsToday);
        model.addAttribute("recentReservations", recentReservations);
        model.addAttribute("lowStockList", lowStockList);

        return "admin/dashboard";
    }

    // --- PRODUCTS CRUD ---
    @GetMapping("/productos")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products-list";
    }

    @GetMapping("/productos/crear")
    public String createProductForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/product-form";
    }

    @PostMapping("/productos/crear")
    public String saveProduct(@Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "admin/product-form";
        }
        try {
            Product p = new Product();
            p.setName(dto.getName());
            p.setSlug(dto.getSlug());
            p.setCategoryId(dto.getCategoryId());
            p.setBrand(dto.getBrand());
            p.setActiveIngredient(dto.getActiveIngredient());
            p.setPresentation(dto.getPresentation());
            p.setDescription(dto.getDescription());
            p.setPrice(dto.getPrice());
            p.setStock(dto.getStock());
            p.setImageUrl(dto.getImageUrl());
            p.setFeatured(dto.isFeatured());
            p.setActive(dto.isActive());
            p.setAdditionalInfo(dto.getAdditionalInfo());
            p.setWarnings(dto.getWarnings());
            
            productService.createProduct(p);
            ra.addFlashAttribute("successMessage", "Producto creado correctamente.");
            return "redirect:/admin/productos";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/product-form";
        }
    }

    @GetMapping("/productos/editar/{id}")
    public String editProductForm(@PathVariable("id") String id, Model model) {
        Product p = productService.getProductById(id);
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setCategoryId(p.getCategoryId());
        dto.setBrand(p.getBrand());
        dto.setActiveIngredient(p.getActiveIngredient());
        dto.setPresentation(p.getPresentation());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setImageUrl(p.getImageUrl());
        dto.setFeatured(p.isFeatured());
        dto.setActive(p.isActive());
        dto.setAdditionalInfo(p.getAdditionalInfo());
        dto.setWarnings(p.getWarnings());

        model.addAttribute("productDTO", dto);
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/product-form";
    }

    @PostMapping("/productos/editar/{id}")
    public String updateProduct(@PathVariable("id") String id, @Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "admin/product-form";
        }
        try {
            Product p = new Product();
            p.setName(dto.getName());
            p.setSlug(dto.getSlug());
            p.setCategoryId(dto.getCategoryId());
            p.setBrand(dto.getBrand());
            p.setActiveIngredient(dto.getActiveIngredient());
            p.setPresentation(dto.getPresentation());
            p.setDescription(dto.getDescription());
            p.setPrice(dto.getPrice());
            p.setStock(dto.getStock());
            p.setImageUrl(dto.getImageUrl());
            p.setFeatured(dto.isFeatured());
            p.setActive(dto.isActive());
            p.setAdditionalInfo(dto.getAdditionalInfo());
            p.setWarnings(dto.getWarnings());

            productService.updateProduct(id, p);
            ra.addFlashAttribute("successMessage", "Producto actualizado correctamente.");
            return "redirect:/admin/productos";
        } catch (Exception ex) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/product-form";
        }
    }

    @PostMapping("/productos/eliminar/{id}")
    public String deleteProduct(@PathVariable("id") String id, RedirectAttributes ra) {
        productService.deactivateOrDeleteProduct(id);
        ra.addFlashAttribute("successMessage", "El producto ha sido desactivado/eliminado correctamente.");
        return "redirect:/admin/productos";
    }

    // --- CATEGORIES CRUD ---
    @GetMapping("/categorias")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories-list";
    }

    @GetMapping("/categorias/crear")
    public String createCategoryForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        return "admin/category-form";
    }

    @PostMapping("/categorias/crear")
    public String saveCategory(@Valid @ModelAttribute("categoryDTO") CategoryDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/category-form";
        }
        try {
            Category c = new Category();
            c.setName(dto.getName());
            c.setSlug(dto.getSlug());
            c.setDescription(dto.getDescription());
            c.setActive(dto.isActive());
            categoryService.createCategory(c);
            ra.addFlashAttribute("successMessage", "Categoría creada correctamente.");
            return "redirect:/admin/categorias";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/category-form";
        }
    }

    @GetMapping("/categorias/editar/{id}")
    public String editCategoryForm(@PathVariable("id") String id, Model model) {
        Category c = categoryService.getCategoryById(id);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setDescription(c.getDescription());
        dto.setActive(c.isActive());

        model.addAttribute("categoryDTO", dto);
        return "admin/category-form";
    }

    @PostMapping("/categorias/editar/{id}")
    public String updateCategory(@PathVariable("id") String id, @Valid @ModelAttribute("categoryDTO") CategoryDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "admin/category-form";
        }
        try {
            Category c = new Category();
            c.setName(dto.getName());
            c.setSlug(dto.getSlug());
            c.setDescription(dto.getDescription());
            c.setActive(dto.isActive());
            categoryService.updateCategory(id, c);
            ra.addFlashAttribute("successMessage", "Categoría actualizada correctamente.");
            return "redirect:/admin/categorias";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/category-form";
        }
    }

    @PostMapping("/categorias/eliminar/{id}")
    public String deleteCategory(@PathVariable("id") String id, RedirectAttributes ra) {
        try {
            categoryService.deleteOrDeactivateCategory(id);
            ra.addFlashAttribute("successMessage", "Categoría eliminada.");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    // --- RESERVATIONS MANAGEMENT ---
    @GetMapping("/apartados")
    public String listReservations(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) ReservationStatus status,
            @RequestParam(value = "dateFilter", required = false) String dateFilter,
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        int pageSize = 15;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if ("oldest".equals(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt");
        } else if ("highestTotal".equals(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "total");
        } else if ("lowestTotal".equals(sort)) {
            sortObj = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "total");
        }

        Page<Reservation> reservationPage = reservationService.searchAndFilterReservations(search, status, dateFilter, PageRequest.of(page, pageSize, sortObj));
        
        // Fetch real KPIs counts from MongoDB database
        long countPending = reservationService.getReservationsCountByStatus(ReservationStatus.PENDIENTE);
        long countConfirmed = reservationService.getReservationsCountByStatus(ReservationStatus.CONFIRMADO);
        long countPrepared = reservationService.getReservationsCountByStatus(ReservationStatus.PREPARADO);
        long countDeliveredToday = reservationService.getReservationsCountDeliveredToday();
        long countCancelled = reservationService.getReservationsCountByStatus(ReservationStatus.CANCELADO);

        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("dateFilter", dateFilter);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("statuses", ReservationStatus.values());

        // Add metrics to view context
        model.addAttribute("countPending", countPending);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countPrepared", countPrepared);
        model.addAttribute("countDeliveredToday", countDeliveredToday);
        model.addAttribute("countCancelled", countCancelled);

        return "admin/reservations-list";
    }

    @PostMapping("/apartados/estado/{id}")
    public String updateReservationStatus(
            @PathVariable("id") String id,
            @RequestParam("status") ReservationStatus status,
            @RequestParam(value = "cancelNotes", required = false) String cancelNotes,
            RedirectAttributes ra) {
        try {
            if (status == ReservationStatus.CANCELADO) {
                reservationService.cancelReservationWithNotes(id, cancelNotes);
                ra.addFlashAttribute("successMessage", "El apartado ha sido cancelado y el stock reintegrado.");
            } else {
                reservationService.updateStatus(id, status);
                ra.addFlashAttribute("successMessage", "Estado de la reserva actualizado a: " + status.getDisplayName());
            }
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/apartados";
    }

    // --- APP CONFIG ---
    @GetMapping("/configuracion")
    public String showConfig(Model model) {
        model.addAttribute("appConfig", appConfigService.getAppConfig());
        return "admin/config";
    }

    @PostMapping("/configuracion")
    public String updateConfig(@ModelAttribute("appConfig") AppConfig config, RedirectAttributes ra) {
        appConfigService.updateAppConfig(config);
        ra.addFlashAttribute("successMessage", "Configuración de la droguería actualizada correctamente.");
        return "redirect:/admin/configuracion";
    }
}
