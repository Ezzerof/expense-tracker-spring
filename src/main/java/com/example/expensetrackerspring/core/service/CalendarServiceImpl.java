package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.DailyExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.DailyExpenseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    public CalendarServiceImpl(UserRepository userRepository, ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public DailyExpenseResponse getDailyExpenses(DailyExpenseRequest dailyExpenseRequest) {
        User user = userRepository.findById(dailyExpenseRequest.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Expense> expensesList = expenseRepository.findByDateAndUserId(dailyExpenseRequest.date(), dailyExpenseRequest.userId());

        BigDecimal currentBalance = user.getBalance();

        BigDecimal totalExpenses = expensesList.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        currentBalance = currentBalance.subtract(totalExpenses);

        return new DailyExpenseResponse(currentBalance, expensesList);
    }
}
