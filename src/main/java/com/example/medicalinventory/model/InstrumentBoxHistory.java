package com.example.medicalinventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instrument_box_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentBoxHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    private HistoryOperation operation;

    private LocalDateTime timestamp;

    private String doctorName;
}

