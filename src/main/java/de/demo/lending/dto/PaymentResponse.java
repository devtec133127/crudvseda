package de.demo.lending.dto;

public class PaymentResponse {
    private long id;
    private String orderId;
    private double amount;
    private String status;

    // Konstruktor, Getter und Setter
    public PaymentResponse(long id, String orderId, double amount, String status) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    // Getter und Setter...
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}