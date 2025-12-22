package com.gmvehicleinout.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailsResponseDto {

    private String vehicleNumber;
    private String ownerName;
    private String mobile;
    private String chassisNumber;

    // ⭐ Add Image URL Fields ⭐
    private String rcFrontPhotoUrl;
    private String rcBackPhotoUrl;
    private String vehiclePhotoUrl;
    private String idCardFrontPhotoUrl;
    private String idCardBackPhotoUrl;
}
