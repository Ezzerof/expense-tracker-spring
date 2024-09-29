package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.DailyTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.DailyTransactionResponse;

public interface CalendarService {

    DailyTransactionResponse calculateDailyExpenses (DailyTransactionRequest dailyTransactionRequest);
}
