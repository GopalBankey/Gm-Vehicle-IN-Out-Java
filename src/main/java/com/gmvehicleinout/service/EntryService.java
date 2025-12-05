package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.entity.*;
import com.gmvehicleinout.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EntryService {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    private FileUploadService fileUploadService;


    // --------------------------------------------
    //  IN ENTRY
    // --------------------------------------------
    public ResponseEntity<ApiResponse<EntryResponseDto>> addEntry(
            EntryDto entryDto,
            MultipartFile rcPhoto,
            MultipartFile vehiclePhoto,
            MultipartFile idCardPhoto
    ) {

        // Logged-in user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        // ----- FIND OR CREATE VEHICLE -----
        VehicleDetails vehicle = vehicleDetailsRepository
                .findById(entryDto.getVehicleNumber())
                .orElse(new VehicleDetails());

        vehicle.setVehicleNumber(entryDto.getVehicleNumber());
        vehicle.setOwnerName(entryDto.getOwnerName());
        vehicle.setMobile(entryDto.getMobile());
        vehicle.setChassisNumber(entryDto.getChassisNumber());

        // SAVE IMAGES INSIDE VEHICLE DETAILS
        String rcFile = fileUploadService.saveFile(rcPhoto);
        String vehicleFile = fileUploadService.saveFile(vehiclePhoto);
        String idCardFile = fileUploadService.saveFile(idCardPhoto);

        if (rcFile != null) vehicle.setRcPhoto(rcFile);
        if (vehicleFile != null) vehicle.setVehiclePhoto(vehicleFile);
        if (idCardFile != null) vehicle.setIdCardPhoto(idCardFile);

        // Save updated vehicle record
        vehicleDetailsRepository.save(vehicle);

        // ----- PREVENT DUPLICATE ACTIVE ENTRY -----
        Entry activeEntry = entryRepository
                .findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(entryDto.getVehicleNumber(), loggedUser)
                .orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // ----- CREATE NEW ENTRY -----
        Entry entry = new Entry();
        entry.setVehicle(vehicle);
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setUser(loggedUser);
        entry.setDriverName(entryDto.getDriverName());

        entryRepository.save(entry);

        // ----- PREPARE FULL URLs -----
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        String rcUrl = vehicle.getRcPhoto() != null ? baseUrl + vehicle.getRcPhoto() : null;
        String vehicleUrl = vehicle.getVehiclePhoto() != null ? baseUrl + vehicle.getVehiclePhoto() : null;
        String idCardUrl = vehicle.getIdCardPhoto() != null ? baseUrl + vehicle.getIdCardPhoto() : null;

        // ----- RESPONSE DTO -----
        EntryResponseDto response = new EntryResponseDto(
                entry.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getOwnerName(),
                vehicle.getMobile(),
                vehicle.getChassisNumber(),
                entry.getLocation(),
                entry.isKey(),
                entry.getInTime(),
                entry.getOutTime(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                rcUrl,
                vehicleUrl,
                idCardUrl,
                entry.getDriverName()

        );

        return ResponseEntity.ok(new ApiResponse<>("Vehicle Added Successfully", true, response));
    }



    // --------------------------------------------
    //  OUT ENTRYa
    // --------------------------------------------
    public ResponseEntity<ApiResponse> outEntry(String vehicleNumber) {

        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        Entry activeEntry = entryRepository
                .findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(vehicleNumber, loggedUser)
                .orElse(null);

        if (activeEntry == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Vehicle not found or already out", false));
        }

        activeEntry.setOutTime(LocalDateTime.now());
        entryRepository.save(activeEntry);

        return ResponseEntity.ok(new ApiResponse<>("Vehicle Out Successfully", true));
    }


    // --------------------------------------------
    //  GET ALL ENTRIES
    // --------------------------------------------
    public ResponseEntity<ApiResponse<List<EntryResponseDto>>> getAllEntry() {

        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        List<Entry> entries = entryRepository.findByUser(loggedUser);

        // Public base URL on Railway
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        List<EntryResponseDto> dtos = entries.stream().map(entry -> {

            VehicleDetails v = entry.getVehicle();

            String rcUrl = v.getRcPhoto() != null ? baseUrl + v.getRcPhoto() : null;
            String vehicleUrl = v.getVehiclePhoto() != null ? baseUrl + v.getVehiclePhoto() : null;
            String idCardUrl = v.getIdCardPhoto() != null ? baseUrl + v.getIdCardPhoto() : null;

            return new EntryResponseDto(
                    entry.getId(),
                    v.getVehicleNumber(),
                    v.getOwnerName(),
                    v.getMobile(),
                    v.getChassisNumber(),
                    entry.getLocation(),
                    entry.isKey(),
                    entry.getInTime(),
                    entry.getOutTime(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt(),
                    rcUrl,
                    vehicleUrl,
                    idCardUrl,
                    entry.getDriverName()
            );
        }).toList();

        return ResponseEntity.ok(new ApiResponse<>("All Entries", true, dtos));
    }
}
