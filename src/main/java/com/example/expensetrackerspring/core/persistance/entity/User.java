package com.example.expensetrackerspring.core.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class User {
    @Id
    private Long id;
    private String firstName;
    private String username;
    private String password;
    private String email;

    public User() {

    }
}
