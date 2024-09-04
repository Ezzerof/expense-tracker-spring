package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.exceptions.DuplicateIncomeException;
import com.example.expensetrackerspring.core.exceptions.IncomeNotFoundException;
import com.example.expensetrackerspring.core.exceptions.InvalidIncomeDetailsException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Income;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.IncomeRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.*;
import com.example.expensetrackerspring.rest.payload.response.IncomeResponse;
import com.example.expensetrackerspring.rest.payload.response.MonthlySummaryResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveIncomeResponse;
import com.example.expensetrackerspring.rest.payload.response.WeeklySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;

    public IncomeServiceImpl(UserRepository userRepository, IncomeRepository incomeRepository) {
        this.userRepository = userRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    public void saveIncome(IncomeRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (request.name() == null || request.amount() == null) {
            throw new InvalidIncomeDetailsException("Name and amount are required fields.");
        }

        if (request.startDate() == null) {
            throw new InvalidIncomeDetailsException("Start date is required.");
        }

        if (request.frequency() == null) {
            throw new InvalidIncomeDetailsException("Recurrence frequency is required.");
        }

        if (request.frequency() == RecurrenceFrequency.SINGLE) {
            if (request.endDate() != null && !request.endDate().equals(request.startDate())) {
                throw new InvalidIncomeDetailsException("For single incomes, end date must be the same as start date.");
            }
        } else {
            if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
                throw new InvalidIncomeDetailsException("End date must be after start date for recurring incomes.");
            }
        }

        if (incomeExists(request.name())) {
            throw new DuplicateIncomeException("Income already exists");
        }

        Income income = new Income();
        income.setName(request.name());
        income.setDescription(request.description());
        income.setCategory(request.category());
        income.setAmount(request.amount());
        income.setStartDate(request.startDate());
        income.setEndDate(request.endDate());
        income.setRecurrenceFrequency(request.frequency());
        income.setUser(user);

        incomeRepository.save(income);

        if (request.frequency() != RecurrenceFrequency.SINGLE) {
            generateRecurringIncome(income);
        }
    }

    @Override
    public Optional<IncomeResponse> getIncome(GetIncomeRequest request, Long userId) {
        return incomeRepository.findByIdAndUserId(request.id(), userId)
                .map(income -> new IncomeResponse(
                        income.getId(),
                        income.getName(),
                        income.getDescription(),
                        income.getAmount(),
                        income.getCategory(),
                        income.getStartDate(),
                        income.getEndDate(),
                        income.getRecurrenceFrequency()
                ));
    }

    @Override
    public Page<IncomeResponse> getAllIncomes(Pageable pageable, Long userId) {
        return incomeRepository.findByUserId(userId, pageable)
                .map(income -> new IncomeResponse(
                        income.getId(),
                        income.getName(),
                        income.getDescription(),
                        income.getAmount(),
                        income.getCategory(),
                        income.getStartDate(),
                        income.getEndDate(),
                        income.getRecurrenceFrequency()
                ));
    }

    @Override
    public Optional<IncomeResponse> editIncome(IncomeRequest incomeRequest, Long userId) {
        if (incomeRequest == null || incomeRequest.name() == null || incomeRequest.amount() == null) {
            throw new InvalidIncomeDetailsException("Income details are required and cannot be null.");
        }

        Income income = incomeRepository.findByIdAndUserId(incomeRequest.id(), userId).orElseThrow(
                () -> new IncomeNotFoundException("Income not found or access denied.")
        );

        if (incomeRequest.startDate() == null) {
            throw new InvalidIncomeDetailsException("Start date is required.");
        }

        if (incomeRequest.frequency() == RecurrenceFrequency.SINGLE) {
            if (incomeRequest.endDate() != null && !incomeRequest.endDate().equals(incomeRequest.startDate())) {
                throw new InvalidIncomeDetailsException("For single incomes, end date must be the same as start date or null.");
            }
        } else {
            if (incomeRequest.endDate() != null && incomeRequest.endDate().isBefore(incomeRequest.startDate())) {
                throw new InvalidIncomeDetailsException("End date must be after start date for recurring incomes.");
            }
        }

        if (incomeRequest.frequency() == null) {
            throw new InvalidIncomeDetailsException("Recurrence frequency is required.");
        }

        income.setName(incomeRequest.name());
        income.setStartDate(incomeRequest.startDate());
        income.setEndDate(incomeRequest.endDate());
        income.setRecurrenceFrequency(incomeRequest.frequency());
        income.setCategory(incomeRequest.category());
        income.setDescription(incomeRequest.description());
        income.setAmount(incomeRequest.amount());

        incomeRepository.save(income);

        return Optional.of(convertToDto(income));
    }

    @Override
    public RemoveIncomeResponse deleteIncome(RemoveIncomeRequest request, Long userId) {
        Income income = incomeRepository.findByIdAndUserId(request.id(), userId)
                .orElseThrow(() -> new IncomeNotFoundException("Income not found or access denied"));

        if (income.getRecurrenceFrequency() != RecurrenceFrequency.SINGLE) {
            incomeRepository.deleteAllByUserIdAndNameAndDateAfter(userId, income.getName(), income.getStartDate());
        } else {
            incomeRepository.delete(income);
        }
        return new RemoveIncomeResponse(true, "Income deleted successfully");
    }

    private IncomeResponse convertToDto(Income income) {
        return new IncomeResponse(
                income.getId(),
                income.getName(),
                income.getDescription(),
                income.getAmount(),
                income.getCategory(),
                income.getStartDate(),
                income.getEndDate(),
                income.getRecurrenceFrequency()
        );
    }

    @Override
    public void generateRecurringIncome(Income income) {
        LocalDate nextDate = income.getStartDate();
        LocalDate endDate = (income.getEndDate() != null) ? income.getEndDate() : LocalDate.now().plusYears(1);

        while (nextDate.isBefore(endDate)) {
            nextDate = getNextOccurrenceDate(nextDate, income.getRecurrenceFrequency());
            if (nextDate.isBefore(endDate)) {
                Income recurringIncome = new Income();
                recurringIncome.setName(income.getName());
                recurringIncome.setDescription(income.getDescription());
                recurringIncome.setCategory(income.getCategory());
                recurringIncome.setAmount(income.getAmount());
                recurringIncome.setStartDate(nextDate);
                recurringIncome.setRecurrenceFrequency(income.getRecurrenceFrequency());
                recurringIncome.setUser(income.getUser());

                incomeRepository.save(recurringIncome);
            }
        }
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(MonthlySummaryRequest monthlySummaryRequest) {
        User user = userRepository.findById(monthlySummaryRequest.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        BigDecimal totalIncome = incomeRepository.findIncomesByUserIdAndMonthAndYear(user.getId(), monthlySummaryRequest.month(), monthlySummaryRequest.year())
                .stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlySummaryResponse(totalIncome);
    }

    @Override
    public WeeklySummaryResponse getWeeklySummary(WeeklySummaryRequest weeklySummaryRequest) {
        return null;
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


    private boolean incomeExists(String incomeName) {
        return incomeRepository.findByName(incomeName).isPresent();
    }

}
