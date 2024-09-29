package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    Optional<Income> findByName(String name);

    Page<Income> findByUserId(Long userId, Pageable pageable);

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    List<Income> findByDateAndUserId(LocalDate date, Long userId);
}
