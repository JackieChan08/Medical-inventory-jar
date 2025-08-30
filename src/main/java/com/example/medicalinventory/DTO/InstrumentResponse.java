package com.example.medicalinventory.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class InstrumentResponse {
    private UUID id;
    private String barcode;
    private String name;
    private String serialNumber;
    private LocalDate productionDate;
    private LocalDate acceptanceDate;
    private String productionCompany;
    private String country;
    private String composition;
    private Boolean reusable;
    private Integer usageCount;
    private String status;

    private List<FileResponse> images; // фото инструмента
}
