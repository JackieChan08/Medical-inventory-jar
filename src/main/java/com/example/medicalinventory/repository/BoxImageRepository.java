package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.BoxImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoxImageRepository extends JpaRepository<BoxImage, UUID> {
}
