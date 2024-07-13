package com.example.expensetrackerspring.core.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
public class Expense {
    @Id
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal amount;
    private LocalDate date;

    public Expense() {

    }

}
