package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

}
