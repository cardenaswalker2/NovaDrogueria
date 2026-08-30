package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ReservationDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String customerName;

    @NotBlank(message = "El número de teléfono es obligatorio.")
    @Pattern(regexp = "^[+0-9\\s-]{7,15}$", message = "Por favor ingrese un número de teléfono válido.")
    private String customerPhone;

    private String customerEmail;
    
    private String notes;

    @NotBlank(message = "Debe especificar el producto.")
    private String productId;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad mínima a apartar es 1 unidad.")
    private Integer quantity;

    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
