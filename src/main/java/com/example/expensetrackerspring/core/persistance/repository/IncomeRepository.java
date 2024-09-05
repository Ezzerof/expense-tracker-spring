package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    Optional<Income> findByName(String name);

    List<Income> findByUserId(Long userId);
    Page<Income> findByUserId(Long userId, Pageable pageble);

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    List<Income> findByDateAndUserId(LocalDate date, Long userId);
    void deleteAllByUserIdAndNameAndDateAfter(Long userId, String name, LocalDate startDate);

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId AND ((MONTH(i.startDate) <= :month AND YEAR(i.startDate) = :year) OR (MONTH(i.endDate) >= :month AND YEAR(i.endDate) = :year))")
    List<Income> findIncomesByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId AND i.date >= :startOfWeek AND i.date <= :endOfWeek")
    List<Income> findWeeklyIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

}
