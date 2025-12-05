package com.gmvehicleinout.repository;

import com.gmvehicleinout.dto.VehicleDetailsResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.entity.VehicleDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    public ResponseEntity<ApiResponse<VehicleDetailsResponseDto>> getVehicleDetails(String vehicleNumber) {

        VehicleDetails vehicle = vehicleDetailsRepository.findById(vehicleNumber)
                .orElse(null);

        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Vehicle not found", false));
        }
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        VehicleDetailsResponseDto dto = new VehicleDetailsResponseDto(
                vehicle.getVehicleNumber(),
                vehicle.getOwnerName(),
                vehicle.getMobile(),
                vehicle.getChassisNumber(),
              baseUrl +  vehicle.getRcPhoto(),
               baseUrl + vehicle.getVehiclePhoto(),
                baseUrl + vehicle.getIdCardPhoto()

        );

        return ResponseEntity.ok(
                new ApiResponse<>("Vehicle Details Found", true, dto)
        );
    }
}
