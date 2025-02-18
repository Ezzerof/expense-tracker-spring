package com.example.expensetrackerspring.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.exceptions.TransactionNotFoundException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.TransactionRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserMonthlySummaryRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.core.service.TransactionServiceImpl;
import com.example.expensetrackerspring.core.service.UserMonthlySummaryService;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.RemoveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMonthlySummaryRepository userMonthlySummaryRepository;
    @Mock
    private UserMonthlySummaryService userMonthlySummaryService;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private SaveTransactionRequest transactionRequest;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        transactionRequest = new SaveTransactionRequest(
                1L, "Groceries", "Bought groceries",
                BigDecimal.valueOf(50), "Food",
                LocalDate.now(), LocalDate.now(),
                RecurrenceFrequency.SINGLE, TransactionType.EXPENSE
        );

        transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .name("Groceries")
                .description("Bought groceries")
                .amount(BigDecimal.valueOf(50))
                .category("Food")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .transactionType(TransactionType.EXPENSE)
                .recurrenceFrequency(RecurrenceFrequency.SINGLE)
                .build();
    }

    @Test
    void saveTransaction_shouldSaveSingleTransactionSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        SaveTransactionResponse response = transactionService.saveTransaction(transactionRequest, 1L);

        assertTrue(response.successful());
        assertEquals("Transaction saved successfully", response.message());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void saveTransaction_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> transactionService.saveTransaction(transactionRequest, 1L));
    }

    @Test
    void saveTransaction_shouldThrowExceptionWhenStartDateIsNull() {
        SaveTransactionRequest invalidRequest = new SaveTransactionRequest(
                1L, "Rent", "Monthly rent",
                BigDecimal.valueOf(500), "Housing",
                null, LocalDate.now(),
                RecurrenceFrequency.SINGLE, TransactionType.EXPENSE
        );

        assertThrows(UserNotFoundException.class,
                () -> transactionService.saveTransaction(invalidRequest, 1L));
    }

    @Test
    void getTransaction_shouldReturnTransactionIfExists() {
        GetTransactionRequest request = new GetTransactionRequest(1L, TransactionType.EXPENSE);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));

        Optional<TransactionResponse> response = transactionService.getTransaction(request, 1L);

        assertTrue(response.isPresent());
        assertEquals("Groceries", response.get().name());
    }

    @Test
    void getTransaction_shouldReturnEmptyIfTransactionDoesNotExist() {
        GetTransactionRequest request = new GetTransactionRequest(1L, TransactionType.EXPENSE);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        Optional<TransactionResponse> response = transactionService.getTransaction(request, 1L);

        assertTrue(response.isEmpty());
    }

    @Test
    void deleteTransaction_shouldDeleteTransactionIfExists() {
        RemoveTransactionRequest request = new RemoveTransactionRequest(1L);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));

        RemoveTransactionResponse response = transactionService.deleteTransaction(request, 1L);

        assertTrue(response.success());
        assertEquals("Transaction deleted successfully", response.message());
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_shouldThrowExceptionIfTransactionNotFound() {
        RemoveTransactionRequest request = new RemoveTransactionRequest(1L);
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.deleteTransaction(request, 1L));
    }
}

