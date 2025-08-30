package com.example.medicalinventory.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ReturnRequest {
    private String boxBarcode;
    private List<String> instrumentBarcodes;
}
