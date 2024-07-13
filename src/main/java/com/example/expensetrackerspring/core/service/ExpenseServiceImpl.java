package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.DuplicateExpenseException;
import com.example.expensetrackerspring.core.exceptions.ExpenseNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.InvalidExpenseDetailsException;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveExpenseResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseServiceImpl(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SaveExpenseResponse saveExpense(ExpenseRequest expenseRequest) {
        if (expenseRequest == null || expenseRequest.name() == null || expenseRequest.amount() == null) {
            throw new InvalidExpenseDetailsException("Invalid expense details");
        }

        if (expenseExists(expenseRequest.name())) {
            throw new DuplicateExpenseException("Expense already exists");
        }

        repository.save(Expense.builder()
                .name(expenseRequest.name())
                .description(expenseRequest.description())
                .amount(expenseRequest.amount())
                .category(expenseRequest.category())
                .date(expenseRequest.date())
                .build());

        return new SaveExpenseResponse(true, "Expense saved");
    }

    @Override
    public Optional<ExpenseResponse> getExpense(GetExpenseRequest getExpenseRequest) {
        if (getExpenseRequest == null) {
            throw new InvalidExpenseDetailsException("Invalid expense details");
        }

        if (repository.findById(getExpenseRequest.id()).isEmpty()) {
            throw new ExpenseNotFoundException("Expense not found");
        }

        Expense expense = repository.findById(getExpenseRequest.id()).get();
        ExpenseResponse expenseResponse = new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );

        return Optional.of(expenseResponse);
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable) {
        Page<Expense> expensePage = repository.findAll(pageable);
        return expensePage.map(this::convertToDto);
    }

    @Override
    public RemoveExpenseResponse deleteExpense(RemoveExpenseRequest request) {
        if (request == null || request.id() == null) {
            throw new InvalidCredentialException("Invalid id");
        }

        Expense expense = repository.findById(request.id())
                .orElseThrow(() -> new InvalidCredentialException("Invalid id"));

        repository.delete(expense);
        return new RemoveExpenseResponse(true, "Expense removed");
    }

    private boolean expenseExists(String expenseName) {
        return repository.getExpenseByName(expenseName).isPresent();
    }

    private ExpenseResponse convertToDto(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }
}
