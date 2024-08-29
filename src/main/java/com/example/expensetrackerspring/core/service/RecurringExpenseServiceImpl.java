package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.persistance.entity.RecurringExpense;
import com.example.expensetrackerspring.rest.payload.request.RecurringExpenseRequest;

import java.time.LocalDate;

public class RecurringExpenseServiceImpl implements RecurringExpenseService {
    @Override
    public void addRecurringExpense(RecurringExpenseRequest request) {

    }

    @Override
    public void generateExpenseInstances(RecurringExpense recurringExpense) {

    }

    @Override
    public LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency) {
        return null;
    }
}
