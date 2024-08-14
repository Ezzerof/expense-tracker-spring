package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.InvalidUsernameException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import com.example.expensetrackerspring.rest.payload.response.SignInResponse;
import com.example.expensetrackerspring.utils.UserInputValidation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void userSignUp(SignUpRequest signUpRequest) {

        if (signUpRequest == null || signUpRequest.username() == null || signUpRequest.password() == null) {
            throw new InvalidCredentialException("Invalid credentials");
        }

        if (userExists(signUpRequest.username())) {
            throw new InvalidUsernameException("Username already exists");
        }

        if (!UserInputValidation.validateUser(signUpRequest)) {
            throw new InvalidCredentialException("Invalid credentials");
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        userRepository.save(User.builder()
                .firstName(signUpRequest.firstName())
                .email(signUpRequest.email())
                .username(signUpRequest.username())
                .password(encodedPassword)
                .build());
    }

    @Override
    public SignInResponse userSignIn(SignInRequest signInRequest) {

        if (signInRequest == null || signInRequest.username() == null || signInRequest.password() == null) {
            throw new InvalidCredentialException("Invalid credentials");
        }

        User user = userRepository.findByUsername(signInRequest.username())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(signInRequest.password(), user.getPassword())) {
            throw new InvalidCredentialException("Invalid credentials");
        }

        return new SignInResponse(true, "Successfully logged in");
    }

    private boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
