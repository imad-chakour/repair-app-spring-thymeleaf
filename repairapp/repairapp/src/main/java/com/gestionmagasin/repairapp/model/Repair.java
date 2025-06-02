package com.gestionmagasin.repairapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "repairs")
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;
    private String deviceType;
    private String brand;
    private String model;
    private String issueDescription;

    private Double cost;
    private Double advance;

    @Column(unique = true)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    private RepairStatus status = RepairStatus.EN_COURS;

    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "repairer_id")
    private User repairer;

    // Constructors
    public Repair() {
        this.createdAt = LocalDate.now();
        this.trackingCode = generateTrackingCode();
    }

    public Repair(Long id, String clientName, String deviceType, String brand, String model,
                  String issueDescription, Double cost, Double advance, String uuid,
                  RepairStatus status, LocalDate createdAt, User repairer) {
        this.id = id;
        this.clientName = clientName;
        this.deviceType = deviceType;
        this.brand = brand;
        this.model = model;
        this.issueDescription = issueDescription;
        this.cost = cost;
        this.advance = advance;
        this.trackingCode = uuid != null ? uuid : UUID.randomUUID().toString();
        this.status = status != null ? status : RepairStatus.EN_COURS;
        this.createdAt = createdAt != null ? createdAt : LocalDate.now();
        this.repairer = repairer;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getAdvance() {
        return advance;
    }

    public void setAdvance(Double advance) {
        this.advance = advance;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public User getRepairer() {
        return repairer;
    }

    public void setRepairer(User repairer) {
        this.repairer = repairer;
    }

    // Business methods
    public Double getRemainingPayment() {
        return cost != null && advance != null ? Math.max(0, cost - advance) : 0;
    }

    private String generateTrackingCode() {
        // Format: REP-XXXXX (where X is alphanumeric)
        return "REP-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }

    @Override
    public String toString() {
        return "Repair{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", issueDescription='" + issueDescription + '\'' +
                ", cost=" + cost +
                ", advance=" + advance +
                ", trackingCode='" + trackingCode + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", repairer=" + (repairer != null ? repairer.getId() : "null") +
                '}';
    }
}
