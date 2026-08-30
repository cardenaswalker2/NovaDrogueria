package com.example.demo.model;

public enum ReservationStatus {
    PENDIENTE("Pendiente de Confirmación", "🟡"),
    CONFIRMADO("Confirmado", "🔵"),
    PREPARADO("Listo para Recoger", "🟣"),
    ENTREGADO("Entregado", "🟢"),
    CANCELADO("Cancelado", "🔴");

    private final String displayName;
    private final String emoji;

    ReservationStatus(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() { return displayName; }
    public String getEmoji() { return emoji; }
}
