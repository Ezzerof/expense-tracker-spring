package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;

import java.time.LocalDate;
import java.util.List;

public interface UserMonthlySummaryService {
    void updateDailySummary(LocalDate date, User user);

    UserMonthlySummary getSummaryForDay(LocalDate date, User user);

    List<UserMonthlySummary> getSummaryForMonth(User user, String yearMonth);
}
