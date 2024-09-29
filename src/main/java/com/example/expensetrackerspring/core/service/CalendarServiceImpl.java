package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.Income;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.IncomeRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.DailyTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.DailyTransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    @Autowired
    public CalendarServiceImpl(UserRepository userRepository, ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    public DailyTransactionResponse calculateDailyExpenses(DailyTransactionRequest dailyTransactionRequest) {
//        User user = userRepository.findById(dailyTransactionRequest.userId())
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        List<Expense> expensesList = expenseRepository.findByDateAndUserId(dailyTransactionRequest.date(), dailyTransactionRequest.userId());
//        List<Income> incomeList = incomeRepository.findByDateAndUserId(dailyTransactionRequest.date(), dailyTransactionRequest.userId());
//
//        BigDecimal currentBalance = user.getBalance();
//
//        BigDecimal totalExpenses = expensesList.stream()
//                .map(Expense::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        currentBalance = currentBalance.subtract(totalExpenses);
//
//        return new DailyTransactionResponse(currentBalance, expensesList);
        return null;
    }
}
