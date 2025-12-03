package com.gmvehicleinout.controller;

import com.gmvehicleinout.dto.EntryDto;
import com.gmvehicleinout.dto.EntryResponseDto;
import com.gmvehicleinout.entity.ApiResponse;
import com.gmvehicleinout.service.EntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entry")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @PostMapping("/in-entry")
    public ResponseEntity<ApiResponse<EntryResponseDto>> addEntry(@RequestBody @Valid EntryDto entryDto) {
        return entryService.addEntry(entryDto);

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
