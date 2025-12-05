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

        // Logged user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        // find or save vehicle (your same logic)
        VehicleDetails vehicle = vehicleDetailsRepository
                .findById(entryDto.getVehicleNumber())
                .orElse(new VehicleDetails());

        vehicle.setVehicleNumber(entryDto.getVehicleNumber());
        vehicle.setOwnerName(entryDto.getOwnerName());
        vehicle.setMobile(entryDto.getMobile());
        vehicle.setChassisNumber(entryDto.getChassisNumber());

        vehicleDetailsRepository.save(vehicle);

        // prevent duplicate active entry
        Entry activeEntry = entryRepository
                .findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(
                        entryDto.getVehicleNumber(), loggedUser)
                .orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // ⭐ SAVE IMAGES HERE ⭐
        String rcFile = fileUploadService.saveFile(rcPhoto);
        String vehicleFile = fileUploadService.saveFile(vehiclePhoto);
        String idCardFile = fileUploadService.saveFile(idCardPhoto);

        // Create Entry
        Entry entry = new Entry();
        entry.setVehicle(vehicle);
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setUser(loggedUser);

        entry.setRcPhoto(rcFile);
        entry.setVehiclePhoto(vehicleFile);
        entry.setIdCardPhoto(idCardFile);

        entryRepository.save(entry);

        // response
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
                entry.getRcPhoto(),
                entry.getVehiclePhoto(),
                entry.getIdCardPhoto()
        );

        return ResponseEntity.ok(new ApiResponse<>("Vehicle Added", true, response));
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
        String baseUrl = "https://gm-vehicle-in-out-java-production.up.railway.app/uploads/";


        List<EntryResponseDto> dtos = entries.stream().map(entry -> new EntryResponseDto(
                entry.getId(),
                entry.getVehicle().getVehicleNumber(),
                entry.getVehicle().getOwnerName(),
                entry.getVehicle().getMobile(),
                entry.getVehicle().getChassisNumber(),
                entry.getLocation(),
                entry.isKey(),
                entry.getInTime(),
                entry.getOutTime(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                baseUrl + entry.getRcPhoto(),
                baseUrl + entry.getVehiclePhoto(),
                baseUrl + entry.getIdCardPhoto()

        )).toList();

        return ResponseEntity.ok(
                new ApiResponse<>("All Entries", true, dtos)
        );
    }
}
