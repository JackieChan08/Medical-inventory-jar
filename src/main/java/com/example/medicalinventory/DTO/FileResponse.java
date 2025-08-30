package com.example.medicalinventory.DTO;

import lombok.Data;

@Data
public class FileResponse {
    private String originalName;
    private String uniqueName;
    private String url;
    private String fileType;
}
