package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.exceptions.InvalidTransactionDetailsException;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.RecurringExpense;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.RecurringExpenseRepository;
import com.example.expensetrackerspring.rest.payload.request.RecurringTransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RecurringExpenseServiceImpl implements RecurringExpenseService {

    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;

    @Autowired
    public RecurringExpenseServiceImpl(ExpenseRepository expenseRepository, RecurringExpenseRepository recurringExpenseRepository) {
        this.expenseRepository = expenseRepository;
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    @Override
    public void addRecurringExpense(RecurringTransactionRequest request) {
        if (request == null || request.name() == null || request.startDate() == null || request.endDate() == null) {
            throw new InvalidTransactionDetailsException("Invalid expense details");
        }

        RecurringExpense recurringExpense = RecurringExpense.builder()
                .name(request.name())
                .description(request.description())
                .amount(request.amount())
                .endDate(request.endDate())
                .startDate(request.startDate())
                .user(request.user())
                .recurrenceFrequency(request.recurrenceFrequency())
                .category(request.category())
                .build();

        recurringExpenseRepository.save(recurringExpense);
        generateExpenseInstances(recurringExpense);
    }

    @Override
    public void generateExpenseInstances(RecurringExpense recurringExpense) {
        LocalDate todayDate = LocalDate.now();
        LocalDate nextDate = recurringExpense.getStartDate();

        while (nextDate.isBefore(todayDate.plusMonths(1)) && (recurringExpense.getEndDate() == null || nextDate.isBefore(recurringExpense.getEndDate()))) {
            Expense expense = Expense.builder()
                    .name(recurringExpense.getName())
                    .description(recurringExpense.getDescription())
                    .category(recurringExpense.getCategory())
                    .amount(recurringExpense.getAmount())
                    .user(recurringExpense.getUser())
                    .date(nextDate)
                    .build();

            expenseRepository.save(expense);
            nextDate = getNextOccurrenceDate(nextDate, recurringExpense.getRecurrenceFrequency());
        }

    }

    @Override
    public LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
            default -> throw new IllegalArgumentException("Unknown frequency: " + frequency);
        };
    }
}
