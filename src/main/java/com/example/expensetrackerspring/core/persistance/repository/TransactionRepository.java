package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    Page<Transaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.startDate = :startDate")
    List<Transaction> findByUserAndDate(User user, LocalDate startDate);
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.startDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Transaction> findByUserAndName(User user, String name);
}
