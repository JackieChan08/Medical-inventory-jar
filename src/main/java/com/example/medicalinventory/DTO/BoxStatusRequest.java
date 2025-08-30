package com.example.medicalinventory.DTO;

import com.example.medicalinventory.model.BoxStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoxStatusRequest {
    private String barcode;
    private BoxStatus boxStatus;
    private List<String> instrumentBarcodes;
}
