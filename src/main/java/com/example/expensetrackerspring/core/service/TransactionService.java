package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.RemoveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    SaveTransactionResponse saveTransaction(SaveTransactionRequest saveRequest, Long userId);

    Optional<TransactionResponse> getTransaction(GetTransactionRequest getTransactionRequest, Long userId);

    Page<TransactionResponse> getAllTransactions(Pageable pageable, Long userId, TransactionType transactionType);

    Optional<TransactionResponse> updateTransaction(SaveTransactionRequest saveTransactionRequest, Long userId);

    RemoveTransactionResponse deleteTransaction(RemoveTransactionRequest removeTransactionRequest, Long userId);

    LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency);

    List<TransactionResponse> getTransactionsForMonth(Long userId, String yearMonth);

    List<TransactionResponse> getTransactionsForDay(Long userId, LocalDate date);

    void deleteAllOccurrences(Long id, Long id1);
}
