package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByName(String name);
}
