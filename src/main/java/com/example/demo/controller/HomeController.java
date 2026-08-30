package com.example.demo.controller;

import com.example.demo.model.AppConfig;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.service.AppConfigService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private com.example.demo.util.ColombianCurrencyFormatter currencyFormatter;

    @org.springframework.web.bind.annotation.ModelAttribute("currencyFormatter")
    public com.example.demo.util.ColombianCurrencyFormatter getCurrencyFormatter() {
        return currencyFormatter;
    }

    @GetMapping("/")
    public String home(Model model) {
        AppConfig config = appConfigService.getAppConfig();
        List<Product> featuredProducts = productService.getFeaturedProducts();
        List<Category> categories = categoryService.getActiveCategories();

        model.addAttribute("config", config);
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("categories", categories);
        return "public/home";
    }
}
