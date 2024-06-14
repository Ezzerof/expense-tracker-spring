package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;

public interface AuthenticationService {
    void userSignUp(SignUpRequest signUpRequest);
    User authenticate(SignInRequest signInRequest);
}
