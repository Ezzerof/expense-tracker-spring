package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserIdAndStartDate(Long userId, LocalDate startDate);

    List<Transaction> findAllByUserIdAndStartDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Object> findByNameAndTransactionTypeAndUserId(String transactionName, TransactionType transactionType, Long userId);

    Page<Transaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user.id = :userId AND t.name = :name AND t.amount = :amount " +
            "AND t.category = :category AND t.transactionType = :transactionType " +
            "AND t.recurrenceFrequency = :recurrenceFrequency " +
            "AND t.startDate >= :startDate AND t.endDate <= :endDate")
    void deleteAllByPattern(
            @Param("name") String name,
            @Param("amount") BigDecimal amount,
            @Param("category") String category,
            @Param("transactionType") TransactionType transactionType,
            @Param("recurrenceFrequency") RecurrenceFrequency recurrenceFrequency,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("userId") Long userId
    );



}
