package com.gmvehicleinout.controller;

import com.gmvehicleinout.dto.VehicleServiceDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.service.VehicleMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service")
public class ServicesController {

    @Autowired
    private VehicleMaintenanceService vehicleMaintenanceService;

    // ---------------- ADD SERVICE ----------------
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addVehicleService(
            @RequestBody VehicleServiceDto vehicleServiceDto) {

        return vehicleMaintenanceService.addVehicleService(vehicleServiceDto);
    }

    // ---------------- GET ALL SERVICES ----------------
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllVehicleService() {
        return vehicleMaintenanceService.getAllVehicleServices();
    }
}
