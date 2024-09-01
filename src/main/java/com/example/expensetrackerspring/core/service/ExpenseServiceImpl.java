package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.exceptions.DuplicateExpenseException;
import com.example.expensetrackerspring.core.exceptions.ExpenseNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidExpenseDetailsException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public void saveExpense(ExpenseRequest expenseRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (expenseRequest.name() == null || expenseRequest.amount() == null) {
            throw new InvalidExpenseDetailsException("Name and amount are required fields.");
        }

        if (expenseRequest.startDate() == null) {
            throw new InvalidExpenseDetailsException("Start date is required.");
        }

        if (expenseRequest.frequency() == null) {
            throw new InvalidExpenseDetailsException("Recurrence frequency is required.");
        }

        if (expenseRequest.frequency() == RecurrenceFrequency.SINGLE) {
            if (expenseRequest.endDate() != null && !expenseRequest.endDate().equals(expenseRequest.startDate())) {
                throw new InvalidExpenseDetailsException("For single expenses, end date must be the same as start date.");
            }
        } else {
            if (expenseRequest.endDate() != null && expenseRequest.endDate().isBefore(expenseRequest.startDate())) {
                throw new InvalidExpenseDetailsException("End date must be after start date for recurring expenses.");
            }
        }

        if (expenseExists(expenseRequest.name())) {
            throw new DuplicateExpenseException("Expense already exists");
        }

        Expense expense = new Expense();
        expense.setName(expenseRequest.name());
        expense.setDescription(expenseRequest.description());
        expense.setCategory(expenseRequest.category());
        expense.setAmount(expenseRequest.amount());
        expense.setStartDate(expenseRequest.startDate());
        expense.setEndDate(expenseRequest.endDate());
        expense.setRecurrenceFrequency(expenseRequest.frequency());
        expense.setUser(user);

        expenseRepository.save(expense);

        if (expenseRequest.frequency() != RecurrenceFrequency.SINGLE) {
            generateRecurringExpenses(expense);
        }
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
                        expense.getStartDate(),
                        expense.getEndDate(),
                        expense.getRecurrenceFrequency()
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
                        expense.getStartDate(),
                        expense.getEndDate(),
                        expense.getRecurrenceFrequency()
                ));
    }

    @Transactional
    @Override
    public Optional<ExpenseResponse> editExpense(Long id, ExpenseRequest expenseRequest, Long userId) {
        if (expenseRequest == null || expenseRequest.name() == null || expenseRequest.amount() == null) {
            throw new InvalidExpenseDetailsException("Expense details are required and cannot be null.");
        }

        Expense expense = expenseRepository.findByIdAndUserId(expenseRequest.id(), userId).orElseThrow(
                () -> new ExpenseNotFoundException("Expense not found or access denied.")
        );

        if (expenseRequest.startDate() == null) {
            throw new InvalidExpenseDetailsException("Start date is required.");
        }

        if (expenseRequest.frequency() == RecurrenceFrequency.SINGLE) {
            if (expenseRequest.endDate() != null && !expenseRequest.endDate().equals(expenseRequest.startDate())) {
                throw new InvalidExpenseDetailsException("For single expenses, end date must be the same as start date or null.");
            }
        } else {
            if (expenseRequest.endDate() != null && expenseRequest.endDate().isBefore(expenseRequest.startDate())) {
                throw new InvalidExpenseDetailsException("End date must be after start date for recurring expenses.");
            }
        }

        if (expenseRequest.frequency() == null) {
            throw new InvalidExpenseDetailsException("Recurrence frequency is required.");
        }

        expense.setName(expenseRequest.name());
        expense.setStartDate(expenseRequest.startDate());
        expense.setEndDate(expenseRequest.endDate());
        expense.setRecurrenceFrequency(expenseRequest.frequency());
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

        if (expense.getRecurrenceFrequency() != RecurrenceFrequency.SINGLE) {
            expenseRepository.deleteAllByUserIdAndNameAndDateAfter(userId, expense.getName(), expense.getStartDate());
        } else {
            expenseRepository.delete(expense);
        }
        return new RemoveExpenseResponse(true, "Expense deleted successfully");
    }

    @Override
    public void generateRecurringExpenses(Expense expense) {
        LocalDate nextDate = expense.getStartDate();
        LocalDate endDate = (expense.getEndDate() != null) ? expense.getEndDate() : LocalDate.now().plusYears(1);

        while (nextDate.isBefore(endDate)) {
            nextDate = getNextOccurrenceDate(nextDate, expense.getRecurrenceFrequency());
            if (nextDate.isBefore(endDate)) {
                Expense recurringExpense = new Expense();
                recurringExpense.setName(expense.getName());
                recurringExpense.setDescription(expense.getDescription());
                recurringExpense.setCategory(expense.getCategory());
                recurringExpense.setAmount(expense.getAmount());
                recurringExpense.setStartDate(nextDate);
                recurringExpense.setRecurrenceFrequency(expense.getRecurrenceFrequency());
                recurringExpense.setUser(expense.getUser());

                expenseRepository.save(recurringExpense);
            }
        }
    }

    private LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency) {
        switch (frequency) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case YEARLY:
                return currentDate.plusYears(1);
            default:
                throw new IllegalArgumentException("Unknown frequency: " + frequency);
        }
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
                expense.getStartDate(),
                expense.getEndDate(),
                expense.getRecurrenceFrequency()
        );
    }
}
