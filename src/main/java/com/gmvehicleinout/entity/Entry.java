package com.gmvehicleinout.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "in_time", updatable = false)
    private LocalDateTime inTime;

    @Column(name = "out_time")
    private LocalDateTime outTime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Entry belongs to user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
