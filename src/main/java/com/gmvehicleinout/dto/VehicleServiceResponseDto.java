package com.gmvehicleinout.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VehicleServiceResponseDto {
    private long vehicleId;
    private String serviceName;
}
