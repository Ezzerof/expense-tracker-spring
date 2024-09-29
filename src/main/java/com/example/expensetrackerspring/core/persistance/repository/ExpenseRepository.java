package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByName(String name);

    Page<Expense> findByUserId(Long userId, Pageable pageable);
    List<Expense> findByUserId(Long userId);
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND ((MONTH(e.startDate) <= :month AND YEAR(e.startDate) = :year) OR (MONTH(e.endDate) >= :month AND YEAR(e.endDate) = :year))")
    List<Expense> findExpensesByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.startDate >= :startOfWeek AND e.startDate <= :endOfWeek")
    List<Expense> findWeeklyIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
}
