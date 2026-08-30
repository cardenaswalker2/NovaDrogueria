package com.example.demo.model;

import java.time.LocalDateTime;

public class ReservationStatusHistory {
    private ReservationStatus status;
    private LocalDateTime timestamp;
    private String notes;

    // Constructors
    public ReservationStatusHistory() {}

    public ReservationStatusHistory(ReservationStatus status, LocalDateTime timestamp, String notes) {
        this.status = status;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    // Getters and Setters
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
