package com.example.expensetrackerspring.controller;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.service.AuthenticationService;
import com.example.expensetrackerspring.rest.AuthController;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    @Test
    void signUp_ShouldReturnCreated_WhenSignUpIsSuccessful() {
        SignUpRequest signUpRequest = new SignUpRequest("user1", "John", "john@example.com", "password");
        ResponseEntity<String> response = authController.signUp(signUpRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
        verify(authenticationService).userSignUp(signUpRequest);
    }

    @Test
    void signUp_ShouldReturnBadRequest_WhenInvalidCredentials() {
        SignUpRequest signUpRequest = new SignUpRequest("user1", "John", "john@example.com", "password");
        doThrow(new InvalidCredentialException("Invalid credentials"))
                .when(authenticationService).userSignUp(signUpRequest);
        ResponseEntity<String> response = authController.signUp(signUpRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void signIn_ShouldReturnOk_WhenSignInIsSuccessful() {
        SignInRequest signInRequest = new SignInRequest("user1", "password");
        // No exception means successful sign in.
        ResponseEntity<Map<String, String>> response = authController.signIn(signInRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().get("message"));
        verify(authenticationService).userSignIn(signInRequest);
    }

    @Test
    void signIn_ShouldReturnForbidden_WhenUserNotFound() {
        SignInRequest signInRequest = new SignInRequest("user1", "password");
        doThrow(new UserNotFoundException("User not found"))
                .when(authenticationService).userSignIn(signInRequest);
        ResponseEntity<Map<String, String>> response = authController.signIn(signInRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().get("message"));
    }
}

