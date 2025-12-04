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

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
