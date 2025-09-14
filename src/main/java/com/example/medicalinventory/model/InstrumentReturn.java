package com.example.medicalinventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "instrument_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "instrument_id", unique = true)
    private Instrument instrument;

    @Column(name = "return_method")
    @Enumerated(EnumType.STRING)
    private ReturnMethod returnMethod;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "return_date")
    private LocalDate returnDate;

}