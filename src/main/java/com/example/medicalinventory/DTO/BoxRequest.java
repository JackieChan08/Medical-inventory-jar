package com.example.medicalinventory.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BoxRequest {
    private String name;
    private LocalDate returnDate;
    private String nurseName;
    private String department;
    private List<String> instrumentBarcodes;
}

