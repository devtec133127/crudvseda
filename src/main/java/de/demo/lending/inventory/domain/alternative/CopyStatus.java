package de.demo.lending.inventory.domain.alternative;

public enum CopyStatus {
    AVAILABLE,      // Verfügbar für Reservierung
    RESERVED,       // Für einen User reserviert
    ON_LOAN,        // Aktuell ausgeliehen
    DAMAGED,        // Beschädigt, nicht ausleihbar
    LOST            // Verloren gegangen
}