package com.gmvehicleinout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle_details")
public class VehicleDetails {

    @Id
    @Column(name = "vehicle_number")
    private String vehicleNumber;

    private String ownerName;

    private String mobile;

    private String chassisNumber;

    // ⭐ ADD FILE NAME FIELDS HERE ⭐
    private String rcPhoto;
    private String vehiclePhoto;
    private String idCardPhoto;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
