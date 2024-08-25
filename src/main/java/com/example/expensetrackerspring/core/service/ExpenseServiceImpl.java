package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.*;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveExpenseResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SaveExpenseResponse saveExpense(ExpenseRequest expenseRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (expenseRequest.name() == null || expenseRequest.amount() == null) {
            throw new InvalidExpenseDetailsException("Invalid expense details");
        }

        if (expenseExists(expenseRequest.name())) {
            throw new DuplicateExpenseException("Expense already exists");
        }

        expenseRepository.save(Expense.builder()
                .user(user)
                .name(expenseRequest.name())
                .description(expenseRequest.description())
                .amount(expenseRequest.amount())
                .category(expenseRequest.category())
                .date(expenseRequest.date())
                .build());

        return new SaveExpenseResponse(true, "Expense saved");
    }

    @Override
    public Optional<ExpenseResponse> getExpense(GetExpenseRequest getExpenseRequest, Long userId) {
        return expenseRepository.findByIdAndUserId(getExpenseRequest.id(), userId)
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getName(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDate()
                ));
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(Pageable pageable, Long userId) {
        return expenseRepository.findByUserId(userId, pageable)
                .map(expense -> new ExpenseResponse(
                        expense.getId(),
                        expense.getName(),
                        expense.getDescription(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDate()
                ));
    }
    @Transactional
    @Override
    public Optional<ExpenseResponse> editExpense(ExpenseRequest expenseRequest, Long userId) {
        if (expenseRequest == null || expenseRequest.name() == null || expenseRequest.amount() == null) {
            throw new InvalidExpenseDetailsException("Invalid expense details");
        }

        Expense expense = expenseRepository.findByIdAndUserId(expenseRequest.id(), userId).orElseThrow(
                () -> new ExpenseNotFoundException("Expense not found or access denied")
        );

        expense.setName(expenseRequest.name());
        expense.setDate(expenseRequest.date());
        expense.setCategory(expenseRequest.category());
        expense.setDescription(expenseRequest.description());
        expense.setAmount(expenseRequest.amount());
        expenseRepository.save(expense);

        return Optional.of(convertToDto(expense));
    }

    @Override
    public RemoveExpenseResponse deleteExpense(RemoveExpenseRequest request, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(request.id(), userId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found or access denied"));

        expenseRepository.delete(expense);
        return new RemoveExpenseResponse(true, "Expense deleted successfully");
    }


    private boolean expenseExists(String expenseName) {
        return expenseRepository.findByName(expenseName).isPresent();
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
