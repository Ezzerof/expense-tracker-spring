package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.exceptions.DuplicateExpenseException;
import com.example.expensetrackerspring.core.exceptions.ExpenseNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidExpenseDetailsException;
import com.example.expensetrackerspring.core.service.ExpenseService;
import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    @PostMapping
    public ResponseEntity<String> saveExpense(@RequestBody ExpenseRequest expenseRequest) {
        try {
            expenseService.saveExpense(expenseRequest);
            logger.info("Expense saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (InvalidExpenseDetailsException e) {
            logger.error("Invalid expense details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
        catch (DuplicateExpenseException e) {
            logger.error("Expense already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Expense already exists");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getExpense(@PathVariable Long id) {
        try {
            ExpenseResponse expenseResponse = expenseService.getExpense(new GetExpenseRequest(id)).orElseThrow();
            logger.info("Expense retrieved successfully");
            return ResponseEntity.ok(expenseResponse);
        }
        catch (InvalidExpenseDetailsException e) {
            logger.error("Invalid expense details", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid expense details");
        }
        catch (ExpenseNotFoundException e) {
            logger.error("Expense not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found");
        }
    }
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getAllExpenses
            (@RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(defaultValue = "id") String sortBy
             ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ExpenseResponse> expensePage = expenseService.getAllExpenses(pageable);
        logger.info("All expenses retrieved successfully");
        return new ResponseEntity<>(expensePage, HttpStatus.OK);
    }
    @DeleteMapping
    public ResponseEntity<RemoveExpenseResponse> deleteExpense(@PathVariable Long id) {
        RemoveExpenseRequest request = new RemoveExpenseRequest(id);
        RemoveExpenseResponse response = expenseService.deleteExpense(request);
        logger.info("Expense deleted successfully");
        return ResponseEntity.ok(response);
    }
}
