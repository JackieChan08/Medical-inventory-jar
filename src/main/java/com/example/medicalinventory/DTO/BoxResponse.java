package com.example.medicalinventory.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BoxResponse {
    private UUID id;
    private String barcode;
    private String doctorName;
    private String name;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate return_by;
    private LocalDate issued_by;

    private List<InstrumentResponse> instruments;
}
