package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.service.AuthenticationService;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            authenticationService.userSignUp(signUpRequest);
            logger.info("User signed up successfully");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvalidCredentialException e) {
            logger.error("Invalid credentials during sign-up", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Map<String, String>> signIn(@RequestBody SignInRequest signInRequest) {
        try {
            authenticationService.userSignIn(signInRequest);
            logger.info("User signed in successfully");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            logger.error("User not found during sign-in", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "User not found"));
        }
    }
}

