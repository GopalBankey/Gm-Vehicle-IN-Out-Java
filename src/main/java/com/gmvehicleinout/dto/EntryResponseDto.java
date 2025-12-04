package com.gmvehicleinout.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
}
