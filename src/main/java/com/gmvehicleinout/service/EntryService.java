package com.gmvehicleinout.service;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.dto.VehicleServiceResponseDto;
import com.gmvehicleinout.entity.*;
import com.gmvehicleinout.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private  ServicesRepository servicesRepository;


    // --------------------------------------------
    //  IN ENTRY
    // --------------------------------------------
    public ResponseEntity<ApiResponse> addEntry(
            EntryDto entryDto,
            MultipartFile rcFrontPhoto,
            MultipartFile rcBackPhoto,
            MultipartFile vehiclePhoto,
            MultipartFile idCardFrontPhoto,
            MultipartFile idCardBackPhoto
    ) {

        // ===============================
        // LOGGED-IN USER
        // ===============================
        String mobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User loggedUser = userRepository
                .findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===============================
        // FIND OR CREATE VEHICLE
        // ===============================
        VehicleDetails vehicle = vehicleDetailsRepository
                .findById(entryDto.getVehicleNumber())
                .orElse(new VehicleDetails());

        vehicle.setVehicleNumber(entryDto.getVehicleNumber());
        vehicle.setOwnerName(entryDto.getOwnerName());
        vehicle.setMobile(entryDto.getMobile());
        vehicle.setChassisNumber(entryDto.getChassisNumber());

        // ===============================
        // SAVE IMAGES
        // ===============================
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

        vehicleDetailsRepository.save(vehicle);

        // ===============================
        // PREVENT DUPLICATE ACTIVE ENTRY
        // ===============================
        Entry activeEntry = entryRepository
                .findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(
                        entryDto.getVehicleNumber(),
                        loggedUser
                )
                .orElse(null);

        if (activeEntry != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("Vehicle already In", false));
        }

        // ===============================
        // VALIDATE SERVICES
        // ===============================
        if (entryDto.getServiceIds() == null || entryDto.getServiceIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("At least one service is required", false));
        }

        if (entryDto.getServiceIds().contains(-1) &&
                (entryDto.getOtherService() == null ||
                        entryDto.getOtherService().trim().isEmpty())) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Other service is required", false));
        }

        // ===============================
        // CREATE ENTRY
        // ===============================
        Entry entry = new Entry();
        entry.setVehicle(vehicle);
        entry.setUser(loggedUser);
        entry.setLocation(entryDto.getLocation());
        entry.setKey(entryDto.isKey());
        entry.setDriverName(entryDto.getDriverName());
        entry.setNote(entryDto.getNote());
        entry.setInTime(LocalDateTime.now());

        //  FIX: List<Integer> → List<Long>
        entry.setServiceIds(
                entryDto.getServiceIds()
                        .stream()
                        .map(Long::valueOf)
                        .toList()
        );

        entry.setOtherService(entryDto.getOtherService());

        entryRepository.save(entry);

        // ===============================
        // PREPARE IMAGE URLs
        // ===============================
//        String baseUrl =
//                "https://gm-vehicle-in-out-java-production.up.railway.app/files/";
//
//        String rcFrontUrl =
//                vehicle.getRcFrontPhoto() != null ? baseUrl + vehicle.getRcFrontPhoto() : null;
//        String rcBackUrl =
//                vehicle.getRcBackPhoto() != null ? baseUrl + vehicle.getRcBackPhoto() : null;
//        String vehicleUrl =
//                vehicle.getVehiclePhoto() != null ? baseUrl + vehicle.getVehiclePhoto() : null;
//        String idCardFrontUrl =
//                vehicle.getIdCardFrontPhoto() != null ? baseUrl + vehicle.getIdCardFrontPhoto() : null;
//        String idCardBackUrl =
//                vehicle.getIdCardBackPhoto() != null ? baseUrl + vehicle.getIdCardBackPhoto() : null;
//
//        // ===============================
//        // RESPONSE DTO (ALL 20 ARGS)
//        // ===============================
//        EntryResponseDto response = new EntryResponseDto(
//                entry.getId(),                     // 1
//                vehicle.getVehicleNumber(),        // 2
//                vehicle.getOwnerName(),            // 3
//                vehicle.getMobile(),               // 4
//                vehicle.getChassisNumber(),        // 5
//                entry.getLocation(),               // 6
//                entry.isKey(),                     // 7
//                entry.getInTime(),                 // 8
//                entry.getOutTime(),                // 9
//                entry.getCreatedAt(),              // 10
//                entry.getUpdatedAt(),              // 11
//                rcFrontUrl,                        // 12
//                rcBackUrl,                         // 13
//                vehicleUrl,                        // 14
//                idCardFrontUrl,                    // 15
//                idCardBackUrl,                     // 16
//                entry.getDriverName(),             // 17
//                entry.getNote(),                   // 18
//                new ArrayList<>(),                 // 19 services (empty here)
//                entry.getOtherService()             // 20 otherService
//        );

        return ResponseEntity.ok(
                new ApiResponse<>("Vehicle Added Successfully", true)
        );
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

        String mobile = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User loggedUser = userRepository
                .findByMobile(mobile)
                .orElseThrow();

        List<Entry> entries =
                entryRepository.findByUserOrderByInTimeDesc(loggedUser);

        String baseUrl =
                "https://gm-vehicle-in-out-java-production.up.railway.app/files/";

        List<EntryResponseDto> dtos = entries.stream().map(entry -> {

            VehicleDetails v = entry.getVehicle();

            String rcFrontUrl =
                    v.getRcFrontPhoto() != null ? baseUrl + v.getRcFrontPhoto() : null;
            String rcBackUrl =
                    v.getRcBackPhoto() != null ? baseUrl + v.getRcBackPhoto() : null;
            String vehicleUrl =
                    v.getVehiclePhoto() != null ? baseUrl + v.getVehiclePhoto() : null;
            String idCardFrontUrl =
                    v.getIdCardFrontPhoto() != null ? baseUrl + v.getIdCardFrontPhoto() : null;
            String idCardBackUrl =
                    v.getIdCardBackPhoto() != null ? baseUrl + v.getIdCardBackPhoto() : null;

            // ===============================
            // RESOLVE SERVICE IDS → DTOs
            // ===============================
            List<VehicleServiceResponseDto> serviceDtos = new ArrayList<>();

            if (entry.getServiceIds() != null && !entry.getServiceIds().isEmpty()) {

                // ✅ MUST be Long
                List<Long> validServiceIds = entry.getServiceIds()
                        .stream()
                        .filter(id -> !id.equals(-1L)) // exclude "Other"
                        .toList();

                if (!validServiceIds.isEmpty()) {
                    List<com.gmvehicleinout.entity.Service> services =
                            servicesRepository.findByIdIn(validServiceIds);

                    serviceDtos = services.stream()
                            .map(service -> new VehicleServiceResponseDto(
                                    service.getId(),     // Long → long (OK)
                                    service.getName()
                            ))
                            .toList();
                }
            }

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
                    entry.getNote(),
                    serviceDtos,
                    entry.getOtherService()
            );

        }).toList();

        return ResponseEntity.ok(
                new ApiResponse<>("All Entries", true, dtos)
        );
    }
}
