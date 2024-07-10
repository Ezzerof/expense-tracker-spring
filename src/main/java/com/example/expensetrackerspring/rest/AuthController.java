package com.example.expensetrackerspring.rest;

import com.example.expensetrackerspring.core.exceptions.InvalidCredentialException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.service.AuthenticationService;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        try {
            authenticationService.userSignUp(signUpRequest);
        } catch (InvalidCredentialException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest signInRequest) {
        try {
            authenticationService.userSignIn(signInRequest);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }
    }
}

