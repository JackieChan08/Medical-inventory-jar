package com.example.medicalinventory.DTO;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class InstrumentRequest {

    private String name;
    private LocalDate productionDate;
    private LocalDate acceptanceDate;
    private String productionCompany;
    private String country;
    private String composition;
    private Boolean reusable;
    private Integer usageCount;

    private Integer quantity; // количество инструментов для создания за один запрос

    private List<MultipartFile> images;

}

