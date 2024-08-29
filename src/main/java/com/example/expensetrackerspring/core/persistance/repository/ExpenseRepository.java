package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByName(String name);

    Page<Expense> findByUserId(Long userId, Pageable pageable);
    Optional<Expense> findByIdAndUserId(Long id, Long userId);
    List<Expense> findByDateAndUserId(LocalDate date, Long userId);
}
