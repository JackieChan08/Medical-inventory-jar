package com.example.medicalinventory.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "instruments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String barcode;

    private String name;

    private String serialNumber;

    private LocalDate productionDate; // дата изготовления
    private LocalDate acceptanceDate;

    private String productionCompany;
    private String country;
    private String composition;

    private Boolean reusable;
    private Integer usageCount; // сколько раз применялся

    @Enumerated(EnumType.STRING)
    private InstrumentStatus status;

    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("instrument_images")
    private List<InstrumentImage> instrumentImages;

}