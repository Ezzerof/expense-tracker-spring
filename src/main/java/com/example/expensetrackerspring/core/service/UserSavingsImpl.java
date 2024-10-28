package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.response.SavingsResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserSavingsImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserSavingsImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateSavings(Long userId, BigDecimal newSavings) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setBalance(newSavings);
        userRepository.save(user);
    }

    @Transactional
    public SavingsResponse getSavings(Long userId) throws UserNotFoundException {
        return new SavingsResponse(
                userRepository.findById(userId)
                        .map(user -> user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found for ID: " + userId)));
    }

}
