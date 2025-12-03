package com.gmvehicleinout.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    private String mobile;
    private String password;
    private String fullName;
}
