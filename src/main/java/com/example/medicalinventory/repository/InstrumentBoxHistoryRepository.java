package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.InstrumentBoxHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstrumentBoxHistoryRepository extends JpaRepository<InstrumentBoxHistory, UUID> {
}
