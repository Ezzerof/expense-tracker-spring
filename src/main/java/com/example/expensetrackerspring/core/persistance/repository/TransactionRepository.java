package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByStartDateAndUserId(LocalDate startDate, Long userId);

    List<Transaction> findAllByUserIdAndStartDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Object> findByNameAndTransactionTypeAndUserId(String transactionName, TransactionType transactionType, Long userId);

    Page<Transaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType, Pageable pageable);
}
