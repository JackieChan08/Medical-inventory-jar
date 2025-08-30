package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.BoxResponse;
import com.example.medicalinventory.DTO.FileResponse;
import com.example.medicalinventory.DTO.InstrumentResponse;
import com.example.medicalinventory.model.Box;
import com.example.medicalinventory.model.BoxImage;
import com.example.medicalinventory.model.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxConverterService {

    private final InstrumentConverterService instrumentConverterService;

    @Value("${app.base-url}")
    private String baseUrl;

    public BoxResponse convertToBoxResponse(Box box) {
        BoxResponse response = new BoxResponse();

        response.setId(box.getId());
        response.setBarcode(box.getBarcode());
        response.setDoctorName(box.getDoctorName());
        response.setStatus(box.getStatus() != null ? box.getStatus().name() : null);
        response.setCreatedAt(box.getCreatedAt());
        response.setUpdatedAt(box.getUpdatedAt());
        response.setName(box.getName());
        response.setReturn_by(box.getReturnBy());


        // инструменты
        if (box.getInstruments() != null && !box.getInstruments().isEmpty()) {
            List<InstrumentResponse> instruments = box.getInstruments().stream()
                    .map(instrumentConverterService::convertToInstrumentResponse)
                    .toList();
            response.setInstruments(instruments);
        }

        return response;
    }

    private FileResponse mapToFileResponse(FileEntity image) {
        FileResponse fileResponse = new FileResponse();
        fileResponse.setOriginalName(image.getOriginalName());
        fileResponse.setUniqueName(image.getUniqueName());
        fileResponse.setFileType(image.getFileType());
        fileResponse.setUrl(baseUrl + "/uploads/" + image.getUniqueName());
        return fileResponse;
    }
}
