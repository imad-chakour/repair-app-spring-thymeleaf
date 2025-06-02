package com.gestionmagasin.repairapp.dto;

import com.gestionmagasin.repairapp.model.RepairStatus;
import lombok.Data;

@Data
public class RepairRequest {
    private String clientName;
    private String deviceType;
    private String brand;
    private String model;
    private String issueDescription;
    private Double cost;
    private Double advance;
    private RepairStatus status;
}
