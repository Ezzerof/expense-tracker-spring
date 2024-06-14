package com.example.expensetrackerspring.core.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Expense {
    @Id
    int id;
    String name;
    String description;
    String category;
    float amount;
    LocalDateTime payDate;


}
