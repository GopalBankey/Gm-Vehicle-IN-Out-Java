package com.gmvehicleinout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EntryResponseDto {

    private Long id;
    private String vehicleNumber;
    private String ownerName;
    private String mobile;
    private String chassisNumber;
    private String location;
    private boolean key;
    private LocalDateTime inTime;
    private LocalDateTime outTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String rcPhoto;        // NEW
    private String vehiclePhoto;   // NEW
    private String idCardPhoto;

    private  String  driverName;// NEW
}
