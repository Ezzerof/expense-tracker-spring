package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.rest.payload.request.*;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.MonthlySummaryResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.WeeklySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExpenseService {
    void saveExpense(ExpenseRequest expenseRequest, Long userId);

    Optional<ExpenseResponse> getExpense(GetExpenseRequest getExpenseRequest, Long userId);

    Page<ExpenseResponse> getAllExpenses(Pageable pageable, Long userId);

    Optional<ExpenseResponse> editExpense(Long id, ExpenseRequest expenseRequest, Long userId);

    RemoveExpenseResponse deleteExpense(RemoveExpenseRequest request, Long userId);

    void generateRecurringExpenses(Expense expense);

    MonthlySummaryResponse getMonthlySummary(MonthlySummaryRequest monthlySummaryRequest);

    WeeklySummaryResponse getWeeklySummary(WeeklySummaryRequest weeklySummaryRequest);
}
