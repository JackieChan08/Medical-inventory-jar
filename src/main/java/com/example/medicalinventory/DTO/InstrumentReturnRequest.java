package com.example.medicalinventory.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InstrumentReturnRequest {
    private String barcode;
    private String returnMethod; // "RETURNED", "PAID", "REPLACED"
    private BigDecimal sum; // required for "PAID"
}