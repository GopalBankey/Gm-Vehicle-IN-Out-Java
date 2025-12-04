package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.entity.*;
import com.gmvehicleinout.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EntryService {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;


    // --------------------------------------------
    //  IN ENTRY
    // --------------------------------------------
    public ResponseEntity<ApiResponse<EntryResponseDto>> addEntry(EntryDto entryDto) {

        // Logged user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        // Check vehicle Details table
        VehicleDetails vehicle = vehicleDetailsRepository
                .findById(entryDto.getVehicleNumber())
                .orElse(null);

        if (vehicle == null) {
            vehicle = new VehicleDetails();
            vehicle.setVehicleNumber(entryDto.getVehicleNumber());
            vehicle.setOwnerName(entryDto.getOwnerName());
            vehicle.setMobile(entryDto.getMobile());
            vehicle.setChassisNumber(entryDto.getChassisNumber());
        } else {
            if (vehicle.getMobile() == null || !vehicle.getMobile().equals(entryDto.getMobile())) {
                vehicle.setMobile(entryDto.getMobile());
            }

            if (vehicle.getOwnerName() == null || !vehicle.getOwnerName().equals(entryDto.getOwnerName())) {
                vehicle.setOwnerName(entryDto.getOwnerName());
            }

            if (vehicle.getChassisNumber() == null || !vehicle.getChassisNumber().equals(entryDto.getChassisNumber())) {
                vehicle.setChassisNumber(entryDto.getChassisNumber());
            }
        }

        vehicleDetailsRepository.save(vehicle);

        // Check active entry (user + vehicle)
        Entry activeEntry =
                entryRepository.findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(
                        entryDto.getVehicleNumber(), loggedUser
                ).orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // Create new Entry
        Entry entry = new Entry();
        entry.setVehicle(vehicle);
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setUser(loggedUser);

        entryRepository.save(entry);

        EntryResponseDto responseDto = new EntryResponseDto(
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
                entry.getUpdatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Vehicle Added Successfully", true, responseDto));
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
                entry.getUpdatedAt()
        )).toList();

        return ResponseEntity.ok(
                new ApiResponse<>("All Entries", true, dtos)
        );
    }
}
