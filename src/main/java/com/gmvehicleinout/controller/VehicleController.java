package com.gmvehicleinout.controller;

import com.gmvehicleinout.dto.VehicleDetailsResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.repository.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/{vehicleNumber}")
    public ResponseEntity<ApiResponse<VehicleDetailsResponseDto>> getVehicleDetails(
            @PathVariable String vehicleNumber) {

        return vehicleService.getVehicleDetails(vehicleNumber);
    }
}
