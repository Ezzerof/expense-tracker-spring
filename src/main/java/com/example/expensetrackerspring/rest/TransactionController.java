package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.exceptions.DuplicateTransactionException;
import com.example.expensetrackerspring.core.exceptions.TransactionNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidTransactionDetailsException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.service.TransactionService;
import com.example.expensetrackerspring.core.service.UserService;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SavingsRequest;
import com.example.expensetrackerspring.rest.payload.response.SavingsResponse;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveTransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<String> saveTransaction(@RequestBody SaveTransactionRequest saveTransactionRequest, @AuthenticationPrincipal User user) {
        try {
            transactionService.saveTransaction(saveTransactionRequest, user.getId());
            logger.info("Expense {} saved successfully by user {}", saveTransactionRequest.name(), user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (InvalidTransactionDetailsException e) {
            logger.error("Invalid expense details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
        catch (DuplicateTransactionException e) {
            logger.error("Expense already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Expense already exists");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable Long id, @RequestParam TransactionType transactionType, @AuthenticationPrincipal User user) {
        try {
            TransactionResponse transactionResponse = transactionService.getTransaction(new GetTransactionRequest(id, transactionType), user.getId()).orElseThrow();
            logger.info("Expense id {} retrieved successfully by {}", id, user.getUsername());
            return ResponseEntity.ok(transactionResponse);
        }
        catch (InvalidTransactionDetailsException e) {
            logger.error("Invalid expense details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid expense details");
        }
        catch (TransactionNotFoundException e) {
            logger.error("Expense not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found");
        }
    }

    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsForMonth(
            @PathVariable String yearMonth,
            @AuthenticationPrincipal User user) {
        try {
            List<TransactionResponse> transactions = transactionService.getTransactionsForMonth(user.getId(), yearMonth);
            logger.info("Transactions for {} retrieved successfully by user {}", yearMonth, user.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions for {}: ", yearMonth, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsForDay(
            @PathVariable String day,
            @AuthenticationPrincipal User user) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate parsedDate = LocalDate.parse(day, formatter);

            List<TransactionResponse> transactions = transactionService.getTransactionsForDay(user.getId(), parsedDate);
            logger.info("Transactions for {} retrieved successfully by user {}", day, user.getUsername());
            return ResponseEntity.ok(transactions);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format for day {}: ", day, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions for {}: ", day, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping()
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort,
            @RequestParam TransactionType transactionType,
            @AuthenticationPrincipal User user) {
        String[] sortParams = sort.split(",");
        Sort sorting = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<TransactionResponse> expensePage = transactionService.getAllTransactions(pageable, user.getId(), transactionType);

        logger.info("All transactions retrieved successfully");
        return new ResponseEntity<>(expensePage, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody SaveTransactionRequest saveTransactionRequest, @AuthenticationPrincipal User user) {
        try {
            SaveTransactionRequest updatedRequest = new SaveTransactionRequest(
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
            TransactionResponse updatedTransaction = transactionService.updateTransaction(updatedRequest, user.getId()).orElseThrow(() ->
                    new TransactionNotFoundException("Expense not found or access denied"));
            logger.info("Expense {} updated successfully by user {}", id, user.getUsername());
            return ResponseEntity.ok(updatedTransaction);
        } catch (InvalidTransactionDetailsException e) {
            logger.error("Invalid expense details provided for {}", id, e);
            return ResponseEntity.badRequest().body("Invalid expense details");
        } catch (TransactionNotFoundException e) {
            logger.error("Attempt to edit non-existing or unauthorized expense {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(
            @PathVariable Long id,
            @RequestParam boolean deleteAll,
            @AuthenticationPrincipal User user) {

        if (deleteAll) {
            transactionService.deleteAllMatchingRecurringTransactions(id, user.getId());
        } else {
            transactionService.deleteTransaction(new RemoveTransactionRequest(id), user.getId());
        }
        return ResponseEntity.ok().build();
    }


    @PostMapping("/savings")
    public ResponseEntity<?> saveSavings(@RequestBody SavingsRequest savingsRequest, @AuthenticationPrincipal User user) {
        try {
            if (savingsRequest.savings() == null || savingsRequest.savings().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body("Invalid savings value. Savings must be non-negative.");
            }

            userService.updateSavings(user.getId(), savingsRequest.savings());
            return ResponseEntity.ok("Savings updated successfully");
        } catch (Exception e) {
            logger.error("Failed to save savings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update savings");
        }
    }

    @GetMapping("/savings")
    public ResponseEntity<SavingsResponse> getSavings(@AuthenticationPrincipal User user) {
        try {
            SavingsResponse savingsResponse = userService.getSavings(user.getId());
            return ResponseEntity.ok(savingsResponse);
        } catch (UserNotFoundException e) {
            logger.error("User not found: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Failed to retrieve savings: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

