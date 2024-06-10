package com.example.expensetrackerspring.core.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    int id;
    String firstName;
    String username;
    String password;
    String email;
}
