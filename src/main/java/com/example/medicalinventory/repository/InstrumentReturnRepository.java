package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.InstrumentReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstrumentReturnRepository extends JpaRepository<InstrumentReturn, UUID> {
    InstrumentReturn findByInstrumentId(UUID instrumentId);
}