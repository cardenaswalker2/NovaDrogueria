package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductDTO {

    private String id;

    @NotBlank(message = "El nombre del producto es obligatorio.")
    private String name;

    @NotBlank(message = "El slug web es obligatorio.")
    private String slug;

    @NotBlank(message = "La categoría es obligatoria.")
    private String categoryId;

    @NotBlank(message = "La marca es obligatoria.")
    private String brand;

    private String activeIngredient;
    
    @NotBlank(message = "La presentación es obligatoria.")
    private String presentation;

    @NotBlank(message = "La descripción es obligatoria.")
    private String description;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0.")
    private BigDecimal price;

    @NotNull(message = "El stock inicial es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;

    private String imageUrl;
    private boolean featured;
    private boolean active = true;
    private String additionalInfo;
    private String warnings;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }

    public String getPresentation() { return presentation; }
    public void setPresentation(String presentation) { this.presentation = presentation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getWarnings() { return warnings; }
    public void setWarnings(String warnings) { this.warnings = warnings; }
}
