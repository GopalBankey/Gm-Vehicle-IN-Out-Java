package com.gmvehicleinout.entity;

import lombok.Getter;

@Getter

public class ApiResponse<T> {
   private final String message;
   private final boolean success;
   private T data;

   //Constructor Without data
  public ApiResponse(String message, boolean success) {
      this.message = message;
      this.success = success;
  }

  //constructor with data
  public ApiResponse(String message, boolean success, T data) {
      this.message = message;
      this.success = success;
      this.data = data;
  }
}
