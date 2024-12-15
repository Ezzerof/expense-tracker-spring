package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;
import com.example.expensetrackerspring.core.persistance.repository.TransactionRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserMonthlySummaryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class UserMonthlySummaryServiceImpl implements UserMonthlySummaryService {

    private final UserMonthlySummaryRepository userMonthlySummaryRepository;
    private final TransactionRepository transactionRepository;

    public UserMonthlySummaryServiceImpl(UserMonthlySummaryRepository userMonthlySummaryRepository,
                                         TransactionRepository transactionRepository) {
        this.userMonthlySummaryRepository = userMonthlySummaryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    @Override
    public void updateDailySummary(LocalDate date, User user) {
        YearMonth month = YearMonth.from(date);

        LocalDate lastDayOfPreviousMonth = month.minusMonths(1).atEndOfMonth();
        BigDecimal previousMonthSavings = getPreviousDaySavings(user, lastDayOfPreviousMonth);

        for (LocalDate currentDay = month.atDay(1); !currentDay.isAfter(month.atEndOfMonth()); currentDay = currentDay.plusDays(1)) {
            LocalDate finalCurrentDay = currentDay;
            userMonthlySummaryRepository.findByUserAndDate(user, currentDay)
                    .orElseGet(() -> {
                        UserMonthlySummary newSummary = new UserMonthlySummary();
                        newSummary.setUser(user);
                        newSummary.setDate(finalCurrentDay);
                        newSummary.setIncome(BigDecimal.ZERO);
                        newSummary.setExpenses(BigDecimal.ZERO);
                        newSummary.setSavings(finalCurrentDay.equals(month.atDay(1)) ? previousMonthSavings : BigDecimal.ZERO);
                        return userMonthlySummaryRepository.save(newSummary);
                    });
        }

        List<UserMonthlySummary> summaries = userMonthlySummaryRepository.findByUserAndDateBetween(
                user,
                month.atDay(1),
                month.atEndOfMonth()
        );

        BigDecimal previousDaySavings = previousMonthSavings;

        for (UserMonthlySummary summary : summaries) {
            List<Transaction> transactions = transactionRepository.findByUserAndDate(user, summary.getDate());

            BigDecimal totalIncome = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpenses = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            summary.setIncome(totalIncome);
            summary.setExpenses(totalExpenses);
            summary.setSavings(previousDaySavings.add(totalIncome).subtract(totalExpenses));

            userMonthlySummaryRepository.save(summary);

            previousDaySavings = summary.getSavings();
        }
    }


    @Override
    public List<UserMonthlySummary> getSummaryForMonth(User user, String yearMonth) {
        YearMonth month = YearMonth.parse(yearMonth);
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();

        updateDailySummary(startOfMonth, user);

        return userMonthlySummaryRepository.findByUserAndDateBetween(user, startOfMonth, endOfMonth);
    }


    @Override
    public UserMonthlySummary getSummaryForDay(LocalDate date, User user) {
        return userMonthlySummaryRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new RuntimeException("Summary not found for date: " + date));
    }

    private BigDecimal getPreviousDaySavings(User user, LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        return userMonthlySummaryRepository.findByUserAndDate(user, previousDay)
                .map(UserMonthlySummary::getSavings)
                .orElse(BigDecimal.ZERO);
    }
}


