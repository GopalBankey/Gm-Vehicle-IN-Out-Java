package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.VehicleServiceDto;
import com.gmvehicleinout.dto.VehicleServiceResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.entity.Service;
import com.gmvehicleinout.repository.ServicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class VehicleMaintenanceService {

    @Autowired
    private ServicesRepository servicesRepository;

    public ResponseEntity<ApiResponse> addVehicleService(VehicleServiceDto vehicleServiceDto) {
        servicesRepository.findByName(vehicleServiceDto.getServiceName()).ifPresent(service -> {
            throw new RuntimeException("Service already exists");
        });

        Service service = new Service();
        service.setName(vehicleServiceDto.getServiceName());
        servicesRepository.save(service);


        return ResponseEntity.ok(new ApiResponse("Service added successfully", true, service));

    }

    public ResponseEntity<ApiResponse> getAllVehicleServices() {
        List<VehicleServiceResponseDto> services = servicesRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse("All Vehicle Services", true, services));

    }

    VehicleServiceResponseDto mapToDTO(Service service) {
        return new VehicleServiceResponseDto(
                service.getId(),
                service.getName()
        );

    }
}
