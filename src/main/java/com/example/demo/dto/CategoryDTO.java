package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDTO {

    private String id;

    @NotBlank(message = "El nombre de la categoría es obligatorio.")
    private String name;

    @NotBlank(message = "El slug de navegación es obligatorio.")
    private String slug;

    private String description;
    private boolean active = true;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
