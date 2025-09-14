package com.example.medicalinventory.model;

public enum BoxStatus {
    CREATED, // СОЗДАНО
    ISSUED, // ВЫДАН
    RETURNED // ВОЗВРАЩЕН
    ;

    public enum ReturnMethod {
        RETURNED,
        PAID,
        REPLACED
    }
}
