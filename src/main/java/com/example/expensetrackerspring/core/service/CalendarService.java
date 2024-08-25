package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.DailyExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.DailyExpenseResponse;

public interface CalendarService {

    DailyExpenseResponse getDailyExpenses (DailyExpenseRequest dailyExpenseRequest);
}
