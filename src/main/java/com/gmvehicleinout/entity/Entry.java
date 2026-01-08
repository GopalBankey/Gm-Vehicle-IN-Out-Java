package com.gmvehicleinout.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "entries")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to vehicle master table
    @ManyToOne
    @JoinColumn(name = "vehicle_number", referencedColumnName = "vehicle_number", nullable = false)
    private VehicleDetails vehicle;

    private String location;

    @Column(name = "has_key")
    private boolean key;

    private String note;


    @Column(name = "in_time", updatable = false)
    private LocalDateTime inTime;

    @Column(name = "out_time")
    private LocalDateTime outTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // Entry belongs to user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private String driverName;
    @ElementCollection
    @CollectionTable(
            name = "entry_services",
            joinColumns = @JoinColumn(name = "entry_id")
    )
    @Column(name = "service_id")
    private List<Long> serviceIds;

    @Column(name = "other_service")
    private String otherService;

}
