package com.gmvehicleinout.repository;

import com.gmvehicleinout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gmvehicleinout.entity.Entry;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry,Long> {
    List<Entry> findByUser(User user);
    List<Entry> findByVehicleNumberAndUser(String vehicleNumber, User user);

}
