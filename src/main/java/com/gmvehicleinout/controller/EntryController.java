package com.gmvehicleinout.controller;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.service.EntryService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/entry")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @PostMapping(
            value = "/in-entry",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<EntryResponseDto>> addEntry(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "rcPhoto", required = false) MultipartFile rcPhoto,
            @RequestPart(value = "vehiclePhoto", required = false) MultipartFile vehiclePhoto,
            @RequestPart(value = "idCardPhoto", required = false) MultipartFile idCardPhoto
    ) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        EntryDto entryDto = mapper.readValue(dataJson, EntryDto.class);

        return entryService.addEntry(entryDto, rcPhoto, vehiclePhoto, idCardPhoto);
    }


    @PostMapping("/out-entry/{vehicleNumber}")
    public ResponseEntity<ApiResponse> outEntry(@PathVariable String vehicleNumber) {
        return entryService.outEntry(vehicleNumber);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EntryResponseDto>>> getAllEntries() {
        return entryService.getAllEntry();
    }
}
