package com.gmvehicleinout.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailsResponseDto {

    private String vehicleNumber;
    private String ownerName;
    private String mobile;
    private String chassisNumber;
}
