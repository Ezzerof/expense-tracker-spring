package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.entity.UserMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMonthlySummaryRepository extends JpaRepository<UserMonthlySummary, Long> {

    @Query("SELECT s FROM UserMonthlySummary s WHERE s.user = :user AND s.date = :startDate")
    Optional<UserMonthlySummary> findByUserAndDate(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT s FROM UserMonthlySummary s WHERE s.user = :user AND s.date BETWEEN :startDate AND :endDate")
    List<UserMonthlySummary> findByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}

