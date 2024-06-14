package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import com.example.expensetrackerspring.utils.RegisterValidation;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void userSignUp(SignUpRequest signUpRequest) {

        String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        if (RegisterValidation.validateUser(signUpRequest)) {
            User.builder()
                    .firstName(signUpRequest.firstName())
                    .email(signUpRequest.email())
                    .username(signUpRequest.username())
                    .password(encodedPassword)
                    .build();
        }

        throw new InvalidCredentialException("Invalid credentials");
    }

    @Override
    public User authenticate(SignInRequest signInRequest) {
        String username = signInRequest.username();
        if (usernameExists(username)) {
            Optional<User> user = userRepository.findByUsername(username);
            String pass = user.get().getPassword();
            if (pass.equals(signInRequest.password())) {
                return user.get();
            }

            throw new UserNotFoundException("User not found");
        }
        return null;
    }

    private boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
