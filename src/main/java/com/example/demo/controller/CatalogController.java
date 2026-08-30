package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Controller
public class CatalogController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private com.example.demo.util.ColombianCurrencyFormatter currencyFormatter;

    @org.springframework.web.bind.annotation.ModelAttribute("currencyFormatter")
    public com.example.demo.util.ColombianCurrencyFormatter getCurrencyFormatter() {
        return currencyFormatter;
    }

    @GetMapping("/catalogo")
    public String catalog(
            @RequestParam(value = "categoria", required = false) String categoryId,
            @RequestParam(value = "orden", defaultValue = "relevante") String sortOption,
            @RequestParam(value = "buscar", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 12;
        Sort sort = Sort.by("name").ascending(); // fallback

        if ("precio_menor".equals(sortOption)) {
            sort = Sort.by("price").ascending();
        } else if ("precio_mayor".equals(sortOption)) {
            sort = Sort.by("price").descending();
        } else if ("mas_recientes".equals(sortOption)) {
            sort = Sort.by("createdAt").descending();
        }

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Product> productPage;
        
        if (query != null && !query.trim().isEmpty()) {
            String searchRegex = ".*" + query.trim() + ".*";
            Criteria criteria = new Criteria().andOperator(
                Criteria.where("active").is(true),
                new Criteria().orOperator(
                    Criteria.where("name").regex(searchRegex, "i"),
                    Criteria.where("brand").regex(searchRegex, "i"),
                    Criteria.where("activeIngredient").regex(searchRegex, "i")
                )
            );
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                criteria = new Criteria().andOperator(
                    Criteria.where("active").is(true),
                    Criteria.where("categoryId").is(categoryId),
                    new Criteria().orOperator(
                        Criteria.where("name").regex(searchRegex, "i"),
                        Criteria.where("brand").regex(searchRegex, "i"),
                        Criteria.where("activeIngredient").regex(searchRegex, "i")
                    )
                );
            }
            Query searchQ = new Query(criteria).with(pageable);
            long total = mongoTemplate.count(Query.query(criteria), Product.class);
            List<Product> products = mongoTemplate.find(searchQ, Product.class);
            productPage = new org.springframework.data.domain.PageImpl<>(products, pageable, total);
        } else if (categoryId != null && !categoryId.trim().isEmpty()) {
            productPage = productService.getActiveProductsByCategory(categoryId, pageable);
        } else {
            productPage = productService.getActiveProducts(pageable);
        }

        List<Category> categories = categoryService.getActiveCategories();

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedSort", sortOption);
        model.addAttribute("searchQuery", query);

        return "public/catalog";
    }

    @GetMapping("/productos/{slug}")
    public String productDetails(@PathVariable("slug") String slug, Model model) {
        Product product = productService.getProductBySlug(slug);
        Category category = categoryService.getCategoryById(product.getCategoryId());

        model.addAttribute("product", product);
        model.addAttribute("category", category);
        return "public/product-details";
    }
}
