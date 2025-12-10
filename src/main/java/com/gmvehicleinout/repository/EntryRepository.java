package com.gmvehicleinout.repository;

import com.gmvehicleinout.entity.Entry;
import com.gmvehicleinout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByVehicle_VehicleNumberAndUser(String vehicleNumber, User user);

    Optional<Entry> findByVehicle_VehicleNumberAndUserAndOutTimeIsNull(String vehicleNumber, User user);

    List<Entry> findByUser(User user);
    List<Entry> findByUserOrderByInTimeDesc(User user);

}
