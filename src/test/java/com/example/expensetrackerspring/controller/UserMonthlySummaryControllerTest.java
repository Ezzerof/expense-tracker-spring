package com.example.expensetrackerspring.controller;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;
import com.example.expensetrackerspring.core.service.UserMonthlySummaryService;
import com.example.expensetrackerspring.rest.UserMonthlySummaryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMonthlySummaryControllerTest {

    @Mock
    private UserMonthlySummaryService userMonthlySummaryService;

    @InjectMocks
    private UserMonthlySummaryController userMonthlySummaryController;

    private User dummyUser;
    private UserMonthlySummary dummySummary;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("testUser");

        dummySummary = new UserMonthlySummary();
        dummySummary.setUser(dummyUser);
        dummySummary.setDate(LocalDate.of(2025, 2, 21));
    }

    @Test
    void getSummaryForDay_ShouldReturnOkWithSummary() {
        String dateStr = "2025-02-21";
        LocalDate parsedDate = LocalDate.parse(dateStr);
        when(userMonthlySummaryService.getSummaryForDay(parsedDate, dummyUser))
                .thenReturn(dummySummary);

        ResponseEntity<UserMonthlySummary> response = userMonthlySummaryController.getSummaryForDay(dateStr, dummyUser);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(dummySummary, response.getBody());
        verify(userMonthlySummaryService).getSummaryForDay(parsedDate, dummyUser);
    }

    @Test
    void getSummaryForMonth_ShouldReturnOkWithSummaries() {
        String yearMonth = "2025-02";
        List<UserMonthlySummary> summaries = List.of(dummySummary);
        when(userMonthlySummaryService.getSummaryForMonth(dummyUser, yearMonth)).thenReturn(summaries);

        ResponseEntity<List<UserMonthlySummary>> response = userMonthlySummaryController.getSummaryForMonth(yearMonth, dummyUser);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(summaries, response.getBody());
        verify(userMonthlySummaryService).getSummaryForMonth(dummyUser, yearMonth);
    }
}

