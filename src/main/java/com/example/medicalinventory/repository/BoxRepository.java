package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.Box;
import com.example.medicalinventory.model.BoxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface BoxRepository extends JpaRepository<Box, UUID> {
    Optional<Box> findByBarcode(String barcode);

    Page<Box> findAllByStatus(BoxStatus status, Pageable pageable);
    Page<Box> findByReturnByBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
