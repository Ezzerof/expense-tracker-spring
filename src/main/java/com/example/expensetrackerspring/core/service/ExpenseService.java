package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExpenseService {
    SaveExpenseResponse saveExpense(ExpenseRequest expenseRequest, Long userId);

    Optional<ExpenseResponse> getExpense(GetExpenseRequest getExpenseRequest, Long userId);

    Page<ExpenseResponse> getAllExpenses(Pageable pageable, Long userId);

    Optional<ExpenseResponse> editExpense(Long id, ExpenseRequest expenseRequest, Long userId);

    RemoveExpenseResponse deleteExpense(RemoveExpenseRequest request, Long userId);
}
