package com.example.expensetrackerspring.core.persistance.repository;

import com.example.expensetrackerspring.core.persistance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
