package com.gmvehicleinout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleServiceDto {

    @NotBlank(message = "Service name is required")

    private  String serviceName;
    
}
