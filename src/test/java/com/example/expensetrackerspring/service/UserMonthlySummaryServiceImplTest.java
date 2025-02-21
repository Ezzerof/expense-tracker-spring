package com.example.expensetrackerspring.service;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;
import com.example.expensetrackerspring.core.persistance.repository.TransactionRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserMonthlySummaryRepository;
import com.example.expensetrackerspring.core.service.UserMonthlySummaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMonthlySummaryServiceImplTest {

    @Mock
    private UserMonthlySummaryRepository userMonthlySummaryRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private UserMonthlySummaryServiceImpl userMonthlySummaryService;

    private User user;
    private UserMonthlySummary summary;
    private Transaction transaction;
    private LocalDate testDate;
    private YearMonth testMonth;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        testDate = LocalDate.of(2024, 2, 15);
        testMonth = YearMonth.of(2024, 2);

        summary = new UserMonthlySummary();
        summary.setUser(user);
        summary.setDate(testDate);
        summary.setIncome(BigDecimal.ZERO);
        summary.setExpenses(BigDecimal.ZERO);
        summary.setSavings(BigDecimal.ZERO);

        transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(BigDecimal.valueOf(100))
                .transactionType(TransactionType.INCOME)
                .startDate(testDate)
                .build();
    }

    @Test
    void shouldUpdateDailySummary() {
        YearMonth testMonth = YearMonth.from(testDate); // February 2024
        LocalDate lastDayOfPreviousMonth = testMonth.minusMonths(1).atEndOfMonth(); // 2024-01-31
        LocalDate previousDay = lastDayOfPreviousMonth.minusDays(1);   // 2024-01-30

        // Stub for previous day (used in getPreviousDaySavings)
        UserMonthlySummary previousSummary = new UserMonthlySummary();
        previousSummary.setUser(user);
        previousSummary.setDate(previousDay);
        previousSummary.setSavings(BigDecimal.ZERO);

        // Lenient stubbing for daily lookup: if the day equals testDate, return our summary; otherwise, return empty.
        lenient().when(userMonthlySummaryRepository.findByUserAndDate(eq(user), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(1);
                    if (date.equals(testDate)) {
                        return Optional.of(summary);
                    } else if (date.equals(previousDay)) {
                        return Optional.of(previousSummary);
                    }
                    return Optional.empty();
                });


        // Stub the transaction repository for testDate.
        when(transactionRepository.findByUserAndDate(user, testDate))
                .thenReturn(List.of(transaction));

        // Stub for monthly lookup to include our summary.
        when(userMonthlySummaryRepository.findByUserAndDateBetween(
                eq(user), eq(testMonth.atDay(1)), eq(testMonth.atEndOfMonth())))
                .thenReturn(List.of(summary));

        // Execute the updateDailySummary method.
        userMonthlySummaryService.updateDailySummary(testDate, user);

        // Verify that the summary for testDate got updated.
        assertEquals(BigDecimal.valueOf(100), summary.getIncome());
        assertEquals(BigDecimal.ZERO, summary.getExpenses());
        verify(userMonthlySummaryRepository, atLeastOnce()).save(any(UserMonthlySummary.class));
    }



    @Test
    void shouldGetSummaryForMonth() {
        when(userMonthlySummaryRepository.findByUserAndDateBetween(user, testMonth.atDay(1), testMonth.atEndOfMonth()))
                .thenReturn(List.of(summary));

        List<UserMonthlySummary> summaries = userMonthlySummaryService.getSummaryForMonth(user, testMonth.toString());

        assertFalse(summaries.isEmpty());
        assertEquals(1, summaries.size());
        verify(userMonthlySummaryRepository, times(2))
                .findByUserAndDateBetween(user, testMonth.atDay(1), testMonth.atEndOfMonth());
    }

    @Test
    void shouldGetSummaryForDay() {
        when(userMonthlySummaryRepository.findByUserAndDate(user, testDate))
                .thenReturn(Optional.of(summary));

        UserMonthlySummary result = userMonthlySummaryService.getSummaryForDay(testDate, user);

        assertNotNull(result);
        assertEquals(testDate, result.getDate());
        verify(userMonthlySummaryRepository, times(1))
                .findByUserAndDate(user, testDate);
    }
}

