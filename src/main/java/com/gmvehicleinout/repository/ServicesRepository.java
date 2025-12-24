package com.gmvehicleinout.repository;

import com.gmvehicleinout.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ServicesRepository extends JpaRepository<Service,Integer> {

    Optional<Service> findByName(String name);
    List<Service> findByIdIn(List<Long> ids);

}
