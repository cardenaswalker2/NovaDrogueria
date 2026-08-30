package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${novadrogueria.admin.username}")
    private String adminUsername;

    @Value("${novadrogueria.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed admin user if none exists
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setFullName("Administrador Nova");
            admin.setRole("ADMIN");
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println(">>> Usuario administrador inicial creado: " + adminUsername);
            if (adminPassword.equals("admin123")) {
                System.err.println(">>> ¡ADVERTENCIA! Utilizando credencial por defecto admin123. Cambie la contraseña en producción.");
            }
        }

        // 2. Seed basic categories for development/demo
        if (categoryRepository.count() == 0) {
            Category m1 = createCategory("Medicamentos", "medicamentos", "Medicamentos de venta libre y bajo fórmula médica.");
            Category m2 = createCategory("Vitaminas y Suplementos", "vitaminas-suplementos", "Vitaminas, multivitaminas y suplementos dietarios.");
            Category m3 = createCategory("Cuidado Personal", "cuidado-personal", "Artículos de higiene, jabones, cremas y lociones.");
            Category m4 = createCategory("Dispositivos Médicos", "dispositivos-medicos", "Termómetros, tensiómetros, tapabocas y jeringas.");

            categoryRepository.save(m1);
            categoryRepository.save(m2);
            categoryRepository.save(m3);
            categoryRepository.save(m4);

            // Seed sample demo products
            createDemoProduct("Acetaminofén 500 mg", "acetaminofen-500-mg", m1.getId(), "Genfar", "Acetaminofén", "Caja x 100 tabletas", "Alivia el dolor leve y moderado y la fiebre.", new BigDecimal("8500.00"), 50, true);
            createDemoProduct("Ibuprofeno 400 mg", "ibuprofeno-400-mg", m1.getId(), "La Sante", "Ibuprofeno", "Caja x 50 tabletas", "Antiinflamatorio no esteroideo indicado para dolores de cabeza y musculares.", new BigDecimal("9800.00"), 4, false);
            createDemoProduct("Vitamina C 1g", "vitamina-c-1g", m2.getId(), "Redoxon", "Ácido Ascórbico", "Tubo x 10 tabletas efervescentes", "Suplemento vitamínico coadyuvante en el fortalecimiento de defensas.", new BigDecimal("14500.00"), 20, true);
            createDemoProduct("Termómetro Digital", "termometro-digital", m4.getId(), "Omron", "N/A", "Unidad", "Termómetro de lectura rápida y alta precisión.", new BigDecimal("25000.00"), 0, false);
        }
    }

    private Category createCategory(String name, String slug, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        c.setActive(true);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private void createDemoProduct(String name, String slug, String catId, String brand, String activeIngredient, String presentation, String desc, BigDecimal price, int stock, boolean featured) {
        Product p = new Product();
        p.setName(name);
        p.setSlug(slug);
        p.setCategoryId(catId);
        p.setBrand(brand);
        p.setActiveIngredient(activeIngredient);
        p.setPresentation(presentation);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setImageUrl("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?q=80&w=300&auto=format&fit=crop");
        p.setFeatured(featured);
        p.setActive(true);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
    }
}
