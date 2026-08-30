package com.example.demo.model;

import java.math.BigDecimal;

public class ReservationItem {

    private String productId;
    private String productNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private int quantity;

    // Default Constructor
    public ReservationItem() {}

    // Constructor
    public ReservationItem(String productId, String productNameSnapshot, BigDecimal unitPriceSnapshot, int quantity) {
        this.productId = productId;
        this.productNameSnapshot = productNameSnapshot;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) { this.unitPriceSnapshot = unitPriceSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() {
        if (unitPriceSnapshot == null) {
            return BigDecimal.ZERO;
        }
        return unitPriceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }
}
