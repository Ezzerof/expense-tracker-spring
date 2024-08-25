package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.exceptions.DuplicateExpenseException;
import com.example.expensetrackerspring.core.exceptions.ExpenseNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidExpenseDetailsException;
import com.example.expensetrackerspring.core.persistance.entity.User;
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
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<String> saveExpense(@RequestBody ExpenseRequest expenseRequest, @AuthenticationPrincipal User user) {
        try {
            expenseService.saveExpense(expenseRequest, user.getId());
            logger.info("Expense {} saved successfully by user {}", expenseRequest.name(), user.getUsername());
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
    public ResponseEntity<?> getExpense(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            ExpenseResponse expenseResponse = expenseService.getExpense(new GetExpenseRequest(id), user.getId()).orElseThrow();
            logger.info("Expense id {} retrieved successfully by {}", id, user.getUsername());
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
    public ResponseEntity<PagedModel<EntityModel<ExpenseResponse>>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String sort,
            @AuthenticationPrincipal User user,
            PagedResourcesAssembler<ExpenseResponse> assembler) {

        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(sortParams[1]).orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        Page<ExpenseResponse> expensePage = expenseService.getAllExpenses(pageable, user.getId());

        PagedModel<EntityModel<ExpenseResponse>> pagedModel = assembler.toModel(expensePage);

        logger.info("All expenses retrieved successfully for user {}, page: {}, size: {}", user.getUsername(), page, size);
        return ResponseEntity.ok(pagedModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editExpense(@PathVariable Long id, @RequestBody ExpenseRequest expenseRequest, @AuthenticationPrincipal User user) {
        try {
            ExpenseResponse updatedExpense = expenseService.editExpense(expenseRequest, user.getId()).orElseThrow(() ->
                    new ExpenseNotFoundException("Expense not found or access denied"));
            logger.info("Expense {} updated successfully by user {}", id, user.getUsername());
            return ResponseEntity.ok(updatedExpense);
        } catch (InvalidExpenseDetailsException e) {
            logger.error("Invalid expense details provided for {}", id, e);
            return ResponseEntity.badRequest().body("Invalid expense details");
        } catch (ExpenseNotFoundException e) {
            logger.error("Attempt to edit non-existing or unauthorized expense {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<RemoveExpenseResponse> deleteExpense(@PathVariable Long id, @AuthenticationPrincipal User user) {
        RemoveExpenseRequest request = new RemoveExpenseRequest(id);
        RemoveExpenseResponse response = expenseService.deleteExpense(request, user.getId());
        logger.info("Expense id {} deleted successfully by user {}", id, user.getUsername());
        return ResponseEntity.ok(response);
    }
}
