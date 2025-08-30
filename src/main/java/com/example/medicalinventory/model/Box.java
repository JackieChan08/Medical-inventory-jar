package com.example.medicalinventory.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "boxes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String barcode;

    private String doctorName;

    @Enumerated(EnumType.STRING)
    private BoxStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(name = "return_by")
    private LocalDate returnBy;



    @ManyToMany
    @JoinTable(
            name = "box_instruments",
            joinColumns = @JoinColumn(name = "box_id"),
            inverseJoinColumns = @JoinColumn(name = "instrument_id")
    )
    private List<Instrument> instruments;


}

