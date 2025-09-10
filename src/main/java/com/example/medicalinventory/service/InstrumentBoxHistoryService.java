package com.example.medicalinventory.service;

import com.example.medicalinventory.model.*;
import com.example.medicalinventory.repository.InstrumentBoxHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InstrumentBoxHistoryService {

    private final InstrumentBoxHistoryRepository historyRepository;

    public void logOperation(Box box, Instrument instrument, HistoryOperation operation, String nurseName, String department) {
        InstrumentBoxHistory history = InstrumentBoxHistory.builder()
                .box(box)
                .instrument(instrument)
                .operation(operation)
                .timestamp(LocalDateTime.now())
                .nurseName(nurseName)
                .department(department)
                .build();
        historyRepository.save(history);
    }
    public void logOperation(Box box, Instrument instrument, HistoryOperation operation) {
        InstrumentBoxHistory history = InstrumentBoxHistory.builder()
                .box(box)
                .instrument(instrument)
                .operation(operation)
                .timestamp(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }
}
