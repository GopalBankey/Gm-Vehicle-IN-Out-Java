package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.entity.Entry;
import com.gmvehicleinout.entity.User;
import com.gmvehicleinout.repository.EntryRepository;
import com.gmvehicleinout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Add Entry (IN)
    public ResponseEntity<ApiResponse<EntryResponseDto>> addEntry(EntryDto entryDto) {

        // Get logged in user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        // fetch all entries for THIS user & vehicle
        List<Entry> entries = entryRepository.findByVehicleNumberAndUser(entryDto.getVehicleNumber(), loggedUser);

        // find active entry (outTime null)
        Entry activeEntry = entries.stream()
                .filter(e -> e.getOutTime() == null)
                .findFirst()
                .orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // Create entry
        Entry entry = new Entry();
        entry.setVehicleNumber(entryDto.getVehicleNumber());
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setMobile(entryDto.getMobile());
        entry.setChassisNumber(entryDto.getChassisNumber());
        entry.setOwnerName(entryDto.getOwnerName());

        entry.setUser(loggedUser);  // <-- IMPORTANT

        entryRepository.save(entry);

        EntryResponseDto entryResponseDto = new EntryResponseDto(
                entry.getId(),
                entry.getVehicleNumber(),
                entry.getInTime(),
                entry.getOutTime(),
                entry.getLocation(),
                entry.isKey(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                entry.getOwnerName(),
                entry.getMobile(),
                entry.getChassisNumber()
        );

        ApiResponse<EntryResponseDto> response =
                new ApiResponse<>("Vehicle Added Successfully", true, entryResponseDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    // Out Entry
    public ResponseEntity<ApiResponse> outEntry(String vehicleNumber) {

        // Get logged in user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        // Fetch entries only for this user
        List<Entry> entries = entryRepository.findByVehicleNumberAndUser(vehicleNumber, loggedUser);

        if (entries.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Vehicle not found", false));
        }

        // Find active IN
        Entry activeEntry = entries.stream()
                .filter(e -> e.getOutTime() == null)
                .findFirst()
                .orElse(null);

        if (activeEntry == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already out", false));
        }

        activeEntry.setOutTime(LocalDateTime.now());
        entryRepository.save(activeEntry);

        return ResponseEntity.ok(new ApiResponse<>("Vehicle Out Successfully", true));
    }


    // Get all entries for logged in user
    public ResponseEntity<ApiResponse<List<EntryResponseDto>>> getAllEntry() {

        // Get logged in user
        String mobile = SecurityContextHolder.getContext().getAuthentication().getName();
        User loggedUser = userRepository.findByMobile(mobile).orElseThrow();

        List<Entry> entries = entryRepository.findByUser(loggedUser);

        List<EntryResponseDto> entryResponseDtos = entries.stream().map(entry -> {
            EntryResponseDto dto = new EntryResponseDto();
            dto.setId(entry.getId());
            dto.setVehicleNumber(entry.getVehicleNumber());
            dto.setLocation(entry.getLocation());
            dto.setKey(entry.isKey());
            dto.setOutTime(entry.getOutTime());
            dto.setInTime(entry.getInTime());
            dto.setCreatedAt(entry.getCreatedAt());
            dto.setUpdatedAt(entry.getUpdatedAt());
            dto.setOwnerName(entry.getOwnerName());
            dto.setMobile(entry.getMobile());
            dto.setChassisNumber(entry.getChassisNumber());
            return dto;
        }).toList();

        ApiResponse<List<EntryResponseDto>> response =
                new ApiResponse<>("All Entries", true, entryResponseDtos);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

