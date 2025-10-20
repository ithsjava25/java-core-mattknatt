package com.example;

import java.time.LocalDate;

public interface Perishable {

    LocalDate expirationDate();

    default boolean isExpired(LocalDate referenceDate) {
        return expirationDate().isBefore(referenceDate);
    }
}
