package com.example.expensetrackerspring.controller;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.service.TransactionService;
import com.example.expensetrackerspring.rest.TransactionController;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.expensetrackerspring.core.RecurrenceFrequency;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private User dummyUser;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("testUser");
    }

    @Test
    void saveTransaction_ShouldReturnCreatedResponse() {
        SaveTransactionRequest request = new SaveTransactionRequest(
                null,
                "Test Transaction",
                "Test Description",
                BigDecimal.valueOf(100),
                "Test Category",
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 21),
                RecurrenceFrequency.SINGLE,
                TransactionType.INCOME
        );

        ResponseEntity<String> response = transactionController.saveTransaction(request, dummyUser);
        verify(transactionService).saveTransaction(request, dummyUser.getId());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Transaction saved successfully", response.getBody());
    }

    @Test
    void getTransaction_ShouldReturnOkWithTransactionResponse() {
        Long transactionId = 1L;
        TransactionResponse dummyResponse = new TransactionResponse(
                transactionId,
                "Test Transaction",
                "Test Description",
                BigDecimal.valueOf(100),
                "Test Category",
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 21),
                TransactionType.INCOME
        );

        when(transactionService.getTransaction(any(GetTransactionRequest.class), eq(dummyUser.getId())))
                .thenReturn(Optional.of(dummyResponse));

        ResponseEntity<TransactionResponse> response = transactionController.getTransaction(transactionId, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dummyResponse, response.getBody());
        verify(transactionService).getTransaction(any(GetTransactionRequest.class), eq(dummyUser.getId()));
    }

    @Test
    void getTransactionsForMonth_ShouldReturnOkWithList() {
        String yearMonth = "2025-02";
        TransactionResponse dummyResponse = new TransactionResponse(
                1L,
                "Test Transaction",
                "Test Description",
                BigDecimal.valueOf(100),
                "Test Category",
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 21),
                TransactionType.INCOME
        );
        List<TransactionResponse> list = List.of(dummyResponse);
        when(transactionService.getTransactionsForMonth(dummyUser.getId(), yearMonth)).thenReturn(list);

        ResponseEntity<List<TransactionResponse>> response = transactionController.getTransactionsForMonth(yearMonth, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(list, response.getBody());
        verify(transactionService).getTransactionsForMonth(dummyUser.getId(), yearMonth);
    }

    @Test
    void getTransactionsForDay_ShouldReturnOkWithList() {
        String day = "2025-02-21";
        LocalDate parsedDate = LocalDate.parse(day);
        TransactionResponse dummyResponse = new TransactionResponse(
                1L,
                "Test Transaction",
                "Test Description",
                BigDecimal.valueOf(100),
                "Test Category",
                parsedDate,
                parsedDate,
                TransactionType.INCOME
        );
        List<TransactionResponse> list = List.of(dummyResponse);
        when(transactionService.getTransactionsForDay(dummyUser.getId(), parsedDate)).thenReturn(list);

        ResponseEntity<List<TransactionResponse>> response = transactionController.getTransactionsForDay(day, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(list, response.getBody());
        verify(transactionService).getTransactionsForDay(dummyUser.getId(), parsedDate);
    }

    @Test
    void updateTransaction_ShouldReturnOkWithUpdatedTransaction() {
        Long transactionId = 1L;
        SaveTransactionRequest request = new SaveTransactionRequest(
                null,
                "Updated Transaction",
                "Updated Description",
                BigDecimal.valueOf(150),
                "Updated Category",
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 21),
                RecurrenceFrequency.SINGLE,
                TransactionType.INCOME
        );
        TransactionResponse updatedResponse = new TransactionResponse(
                transactionId,
                "Updated Transaction",
                "Updated Description",
                BigDecimal.valueOf(150),
                "Updated Category",
                LocalDate.of(2025, 2, 21),
                LocalDate.of(2025, 2, 21),
                TransactionType.INCOME
        );
        when(transactionService.updateTransaction(any(SaveTransactionRequest.class), eq(dummyUser.getId())))
                .thenReturn(Optional.of(updatedResponse));

        ResponseEntity<TransactionResponse> response = transactionController.updateTransaction(transactionId, request, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedResponse, response.getBody());
        verify(transactionService).updateTransaction(any(SaveTransactionRequest.class), eq(dummyUser.getId()));
    }

    @Test
    void deleteTransaction_ShouldDeleteSingleOccurrence() {
        Long transactionId = 1L;
        String deleteType = "SINGLE";

        ResponseEntity<String> response = transactionController.deleteTransaction(transactionId, deleteType, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction deleted successfully", response.getBody());
        verify(transactionService).deleteTransaction(any(RemoveTransactionRequest.class), eq(dummyUser.getId()));
        verify(transactionService, never()).deleteAllOccurrences(anyLong(), anyLong());
    }

    @Test
    void deleteTransaction_ShouldDeleteAllOccurrences() {
        Long transactionId = 1L;
        String deleteType = "ALL";

        ResponseEntity<String> response = transactionController.deleteTransaction(transactionId, deleteType, dummyUser);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transaction deleted successfully", response.getBody());
        verify(transactionService).deleteAllOccurrences(transactionId, dummyUser.getId());
        verify(transactionService, never()).deleteTransaction(any(RemoveTransactionRequest.class), anyLong());
    }

    @Test
    void deleteTransaction_ShouldThrowExceptionForInvalidDeleteType() {
        Long transactionId = 1L;
        String invalidDeleteType = "INVALID";

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionController.deleteTransaction(transactionId, invalidDeleteType, dummyUser)
        );
        assertEquals("Invalid or missing deleteType. Expected 'ALL' or 'SINGLE'.", exception.getMessage());
    }
}

