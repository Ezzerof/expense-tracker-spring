package com.example.expensetrackerspring.service;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.InvalidUsernameException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.core.service.AuthenticationServiceImpl;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import com.example.expensetrackerspring.rest.payload.response.SignInResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void userSignUp_ShouldSaveUser_WhenValidRequest() {
        // Given
        SignUpRequest request = new SignUpRequest("testUser", "John", "john@example.com", "password123");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        // When
        authenticationService.userSignUp(request);

        // Then
        verify(userRepository).save(any(User.class));
    }

    @Test
    void userSignUp_ShouldThrowException_WhenUsernameExists() {
        // Given
        SignUpRequest request = new SignUpRequest("existingUser", "John", "john@example.com", "password123");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(new User()));

        // When / Then
        assertThrows(InvalidUsernameException.class, () -> authenticationService.userSignUp(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void userSignUp_ShouldThrowException_WhenInvalidCredentials() {
        // Given
        SignUpRequest request = new SignUpRequest(null, "John", "john@example.com", "password123");

        // When / Then
        assertThrows(InvalidCredentialException.class, () -> authenticationService.userSignUp(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void userSignIn_ShouldReturnSuccess_WhenValidCredentials() {
        // Given
        SignInRequest request = new SignInRequest("testUser", "password123");
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        // When
        SignInResponse response = authenticationService.userSignIn(request);

        // Then
        assertTrue(response.success());
        assertEquals("Successfully logged in", response.message());
    }

    @Test
    void userSignIn_ShouldThrowException_WhenUserNotFound() {
        // Given
        SignInRequest request = new SignInRequest("nonExistingUser", "password123");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        // When / Then
        assertThrows(UserNotFoundException.class, () -> authenticationService.userSignIn(request));
    }

    @Test
    void userSignIn_ShouldThrowException_WhenPasswordIncorrect() {
        // Given
        SignInRequest request = new SignInRequest("testUser", "wrongPassword");
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // When / Then
        assertThrows(InvalidCredentialException.class, () -> authenticationService.userSignIn(request));
    }
}
