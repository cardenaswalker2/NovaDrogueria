package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reservations")
public class Reservation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String customerName;

    @Indexed
    private String customerPhone;
    
    private String customerEmail;

    private List<ReservationItem> items = new ArrayList<>();
    
    private BigDecimal total;

    @Indexed
    private ReservationStatus status = ReservationStatus.PENDIENTE;

    private String notes;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    private List<ReservationStatusHistory> statusHistory = new ArrayList<>();

    // Getters and Setters
    public List<ReservationStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<ReservationStatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<ReservationItem> getItems() { return items; }
    public void setItems(List<ReservationItem> items) { this.items = items; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getJsonData() {
        com.example.demo.util.ColombianCurrencyFormatter formatter = new com.example.demo.util.ColombianCurrencyFormatter();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"code\":\"").append(code).append("\",");
        sb.append("\"customerName\":\"").append(customerName != null ? customerName.replace("\"", "\\\"") : "").append("\",");
        sb.append("\"customerPhone\":\"").append(customerPhone != null ? customerPhone.replace("\"", "\\\"") : "").append("\",");
        sb.append("\"customerEmail\":\"").append(customerEmail != null ? customerEmail.replace("\"", "\\\"") : "").append("\",");
        sb.append("\"notes\":\"").append(notes != null ? notes.replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\",");
        sb.append("\"total\":\"").append(formatter.format(total)).append("\",");
        sb.append("\"totalInWords\":\"").append(formatter.formatToWords(total)).append("\",");
        sb.append("\"status\":\"").append(status != null ? status.name() : "").append("\",");
        sb.append("\"statusDisplayName\":\"").append(status != null ? status.getDisplayName() : "").append("\",");
        sb.append("\"statusEmoji\":\"").append(status != null ? status.getEmoji() : "").append("\",");
        
        String color = "#ffe4e6";
        String txtColor = "#dc2626";
        if (status != null) {
            switch (status) {
                case PENDIENTE: color = "#fef3c7"; txtColor = "#d97706"; break;
                case CONFIRMADO: color = "#e0f2fe"; txtColor = "#0284c7"; break;
                case PREPARADO: color = "#f3e8ff"; txtColor = "#8b5cf6"; break;
                case ENTREGADO: color = "#d1fae5"; txtColor = "#059669"; break;
            }
        }
        sb.append("\"statusColor\":\"").append(color).append("\",");
        sb.append("\"statusTextColor\":\"").append(txtColor).append("\",");
        
        String dateStr = "";
        if (createdAt != null) {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
            dateStr = createdAt.format(dtf);
        }
        sb.append("\"createdAtStr\":\"").append(dateStr).append("\",");
        
        // Serialize items
        sb.append("\"items\":[");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ReservationItem item = items.get(i);
                sb.append("{");
                sb.append("\"productName\":\"").append(item.getProductNameSnapshot() != null ? item.getProductNameSnapshot().replace("\"", "\\\"") : "").append("\",");
                sb.append("\"quantity\":").append(item.getQuantity()).append(",");
                sb.append("\"unitPrice\":\"").append(formatter.format(item.getUnitPriceSnapshot())).append("\",");
                sb.append("\"lineTotal\":\"").append(formatter.format(item.getLineTotal())).append("\"");
                sb.append("}");
                if (i < items.size() - 1) sb.append(",");
            }
        }
        sb.append("],");
        
        // Serialize history
        sb.append("\"history\":[");
        if (statusHistory != null) {
            for (int i = 0; i < statusHistory.size(); i++) {
                ReservationStatusHistory hist = statusHistory.get(i);
                sb.append("{");
                sb.append("\"displayName\":\"").append(hist.getStatus() != null ? hist.getStatus().getDisplayName() : "").append("\",");
                
                String histDateStr = "";
                if (hist.getTimestamp() != null) {
                    java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                    histDateStr = hist.getTimestamp().format(dtf);
                }
                sb.append("\"timestamp\":\"").append(histDateStr).append("\",");
                sb.append("\"notes\":\"").append(hist.getNotes() != null ? hist.getNotes().replace("\"", "\\\"").replace("\n", " ").replace("\r", "") : "").append("\"");
                sb.append("}");
                if (i < statusHistory.size() - 1) sb.append(",");
            }
        }
        sb.append("]");
        
        sb.append("}");
        return sb.toString();
    }
}
