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
            MultipartFile rcFrontPhoto,
            MultipartFile rcBackPhoto,

            MultipartFile vehiclePhoto,
            MultipartFile idCardFrontPhoto,
            MultipartFile idCardBackPhoto
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
        String rcFrontFile = fileUploadService.saveFile(rcFrontPhoto);
        String rcBackFile = fileUploadService.saveFile(rcBackPhoto);
        String vehicleFile = fileUploadService.saveFile(vehiclePhoto);
        String idCardFrontFile = fileUploadService.saveFile(idCardFrontPhoto);
        String idCardBackFile = fileUploadService.saveFile(idCardBackPhoto);

        if (rcFrontFile != null) vehicle.setRcFrontPhoto(rcFrontFile);
        if (rcBackFile != null) vehicle.setRcBackPhoto(rcBackFile);
        if (vehicleFile != null) vehicle.setVehiclePhoto(vehicleFile);
        if (idCardFrontFile != null) vehicle.setIdCardFrontPhoto(idCardFrontFile);
        if (idCardBackFile != null) vehicle.setIdCardBackPhoto(idCardBackFile);


        // ----- PREVENT DUPLICATE ACTIVE ENTRY -----
        Entry activeEntry = entryRepository
                .findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(entryDto.getVehicleNumber(), loggedUser)
                .orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // Save updated vehicle record
        vehicleDetailsRepository.save(vehicle);



        // ----- CREATE NEW ENTRY -----
        Entry entry = new Entry();
        entry.setVehicle(vehicle);
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setUser(loggedUser);
        entry.setInTime(LocalDateTime.now());
        entry.setDriverName(entryDto.getDriverName());
        entry.setNote(entryDto.getNote());

        entryRepository.save(entry);

        // ----- PREPARE FULL URLs -----
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        String rcFrontUrl = vehicle.getRcFrontPhoto() != null ? baseUrl + vehicle.getRcFrontPhoto() : null;
        String rcBackUrl = vehicle.getRcBackPhoto() != null ? baseUrl + vehicle.getRcBackPhoto() : null;
        String vehicleUrl = vehicle.getVehiclePhoto() != null ? baseUrl + vehicle.getVehiclePhoto() : null;
        String idCardFrontUrl = vehicle.getIdCardFrontPhoto() != null ? baseUrl + vehicle.getIdCardFrontPhoto() : null;
        String idCardBackUrl = vehicle.getIdCardBackPhoto() != null ? baseUrl + vehicle.getIdCardBackPhoto() : null;

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
                rcFrontUrl,
                rcBackUrl,
                vehicleUrl,
                idCardFrontUrl,
                idCardBackUrl,
                entry.getDriverName(),
                entry.getNote()

        );

        return ResponseEntity.ok(new ApiResponse<>("Vehicle Added Successfully", true, response));
    }



    // --------------------------------------------
    //  OUT ENTRY
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

        List<Entry> entries = entryRepository.findByUserOrderByInTimeDesc(loggedUser);

        // Public base URL on Railway
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        List<EntryResponseDto> dtos = entries.stream().map(entry -> {

            VehicleDetails v = entry.getVehicle();

            String rcFrontUrl = v.getRcFrontPhoto() != null ? baseUrl + v.getRcFrontPhoto() : null;
            String rcBackUrl = v.getRcBackPhoto() != null ? baseUrl + v.getRcBackPhoto() : null;
            String vehicleUrl = v.getVehiclePhoto() != null ? baseUrl + v.getVehiclePhoto() : null;
            String idCardFrontUrl = v.getIdCardFrontPhoto() != null ? baseUrl + v.getIdCardFrontPhoto() : null;
            String idCardBackUrl = v.getIdCardBackPhoto() != null ? baseUrl + v.getIdCardBackPhoto() : null;

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
                    rcFrontUrl,
                    rcBackUrl,
                    vehicleUrl,
                    idCardFrontUrl,
                    idCardBackUrl,
                    entry.getDriverName(),
                    entry.getNote()
            );
        }).toList();

        return ResponseEntity.ok(new ApiResponse<>("All Entries", true, dtos));
    }
}
