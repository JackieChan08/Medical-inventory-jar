package com.example.medicalinventory.repository;

import com.example.medicalinventory.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByUniqueName(String uniqueName);
    Optional<FileEntity> findByOriginalName(String originalName);
}
