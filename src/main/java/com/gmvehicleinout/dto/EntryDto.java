package com.gmvehicleinout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class EntryDto {

    @NotBlank(message = "Vehicle Number is required")
    private String vehicleNumber;

    private String location;
    private String ownerName;
    private String mobile;
    private String chassisNumber;
    private boolean key;
    private  String driverName;
    private String note;

}
