package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;
import com.example.expensetrackerspring.core.service.UserMonthlySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/summary")
public class UserMonthlySummaryController {

    private final UserMonthlySummaryService userMonthlySummaryService;

    @Autowired
    public UserMonthlySummaryController(UserMonthlySummaryService userMonthlySummaryService) {
        this.userMonthlySummaryService = userMonthlySummaryService;
    }

    @GetMapping("/day/{date}")
    public ResponseEntity<UserMonthlySummary> getSummaryForDay(
            @PathVariable String date,
            @AuthenticationPrincipal User user) {
        LocalDate parsedDate = LocalDate.parse(date);
        UserMonthlySummary summary = userMonthlySummaryService.getSummaryForDay(parsedDate, user);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/month/{yearMonth}")
    public ResponseEntity<List<UserMonthlySummary>> getSummaryForMonth(
            @PathVariable String yearMonth,
            @AuthenticationPrincipal User user) {
        List<UserMonthlySummary> summaries = userMonthlySummaryService.getSummaryForMonth(user, yearMonth);
        return ResponseEntity.ok(summaries);
    }
}

