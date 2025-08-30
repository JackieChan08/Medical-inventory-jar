package com.example.medicalinventory.service;

import com.example.medicalinventory.DTO.FileResponse;
import com.example.medicalinventory.DTO.InstrumentResponse;
import com.example.medicalinventory.model.FileEntity;
import com.example.medicalinventory.model.Instrument;
import com.example.medicalinventory.model.InstrumentImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentConverterService {

    @Value("${app.base-url}")
    private String baseUrl;

    public InstrumentResponse convertToInstrumentResponse(Instrument instrument) {
        InstrumentResponse response = new InstrumentResponse();

        response.setId(instrument.getId());
        response.setBarcode(instrument.getBarcode());
        response.setName(instrument.getName());
        response.setSerialNumber(instrument.getSerialNumber());
        response.setProductionDate(instrument.getProductionDate());
        response.setAcceptanceDate(instrument.getAcceptanceDate());
        response.setProductionCompany(instrument.getProductionCompany());
        response.setCountry(instrument.getCountry());
        response.setComposition(instrument.getComposition());
        response.setReusable(instrument.getReusable());
        response.setUsageCount(instrument.getUsageCount());
        response.setStatus(instrument.getStatus() != null ? instrument.getStatus().name() : null);

        // обработка картинок
        if (instrument.getInstrumentImages() != null && !instrument.getInstrumentImages().isEmpty()) {
            List<FileResponse> images = instrument.getInstrumentImages().stream()
                    .map(InstrumentImage::getImage)
                    .map(this::mapToFileResponse)
                    .toList();
            response.setImages(images);
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
