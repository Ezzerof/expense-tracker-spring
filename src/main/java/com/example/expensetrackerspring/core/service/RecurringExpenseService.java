package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.persistance.entity.RecurringExpense;
import com.example.expensetrackerspring.rest.payload.request.RecurringExpenseRequest;

import java.time.LocalDate;

public interface RecurringExpenseService {

    void addRecurringExpense(RecurringExpenseRequest request);
    void generateExpenseInstances(RecurringExpense recurringExpense);
    LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency);
}
