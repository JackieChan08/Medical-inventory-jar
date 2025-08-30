package com.example.medicalinventory.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "instrument_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    @JsonBackReference
    private Instrument instrument;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private FileEntity image;
}
