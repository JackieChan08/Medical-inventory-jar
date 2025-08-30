package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.Instrument;
import com.example.medicalinventory.model.InstrumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstrumentRepository extends JpaRepository<Instrument, UUID> {

    @Query("SELECT i FROM Instrument i " +
            "WHERE CAST(i.id AS string) = :value " +
            "OR i.name LIKE %:value% " +
            "OR i.serialNumber LIKE %:value% " +
            "OR i.barcode LIKE %:value%")
    Page<Instrument> search(@Param("value") String value, Pageable pageable);

    Optional<Instrument> findByBarcode(String barcode);

    Page<Instrument> findAllByStatus(InstrumentStatus status, Pageable pageable);



}
