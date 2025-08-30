package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.InstrumentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstrumentImageRepository extends JpaRepository<InstrumentImage, UUID> {
    List<InstrumentImageRepository> findByInstrumentId(UUID instrumentId);
}
