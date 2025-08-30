package com.example.medicalinventory.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "box_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoxImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "box_id")
    @JsonBackReference("box_images")
    private Box box;


    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private FileEntity image;
}
