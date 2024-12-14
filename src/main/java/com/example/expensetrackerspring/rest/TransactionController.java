package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.exceptions.TransactionNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.service.TransactionService;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<String> saveTransaction(@RequestBody SaveTransactionRequest saveTransactionRequest, @AuthenticationPrincipal User user) {
        transactionService.saveTransaction(saveTransactionRequest, user.getId());
        logger.info("Transaction {} saved successfully by user {}", saveTransactionRequest.name(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("Transaction saved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id, @AuthenticationPrincipal User user) {
        TransactionResponse transaction = transactionService.getTransaction(new GetTransactionRequest(id, null), user.getId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        logger.info("Transaction id {} retrieved successfully by {}", id, user.getUsername());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsForMonth(
            @PathVariable String yearMonth,
            @AuthenticationPrincipal User user) {
        List<TransactionResponse> transactions = transactionService.getTransactionsForMonth(user.getId(), yearMonth);
        logger.info("Transactions for {} retrieved successfully by user {}", yearMonth, user.getUsername());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsForDay(
            @PathVariable String day,
            @AuthenticationPrincipal User user) {
        LocalDate parsedDate = LocalDate.parse(day);
        List<TransactionResponse> transactions = transactionService.getTransactionsForDay(user.getId(), parsedDate);
        logger.info("Transactions for {} retrieved successfully by user {}", day, user.getUsername());
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody SaveTransactionRequest saveTransactionRequest,
            @AuthenticationPrincipal User user) {
        saveTransactionRequest = new SaveTransactionRequest(
                id,
                saveTransactionRequest.name(),
                saveTransactionRequest.description(),
                saveTransactionRequest.amount(),
                saveTransactionRequest.category(),
                saveTransactionRequest.startDate(),
                saveTransactionRequest.endDate(),
                saveTransactionRequest.recurrenceFrequency(),
                saveTransactionRequest.transactionType()
        );
        TransactionResponse updatedTransaction = transactionService.updateTransaction(saveTransactionRequest, user.getId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        logger.info("Transaction {} updated successfully by user {}", id, user.getUsername());
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String deleteType,
            @AuthenticationPrincipal User user
    ) {
        logger.info("Delete request received: ID = {}, deleteType = {}, User = {}", id, deleteType, user.getUsername());

        if (deleteType == null || (!"ALL".equalsIgnoreCase(deleteType) && !"SINGLE".equalsIgnoreCase(deleteType))) {
            throw new IllegalArgumentException("Invalid or missing deleteType. Expected 'ALL' or 'SINGLE'.");
        }

        if ("ALL".equalsIgnoreCase(deleteType)) {
            transactionService.deleteAllOccurrences(id, user.getId());
        } else {
            transactionService.deleteTransaction(new RemoveTransactionRequest(id), user.getId());
        }

        logger.info("Transaction {} deleted successfully by user {}", id, user.getUsername());
        return ResponseEntity.ok("Transaction deleted successfully");
    }

}


