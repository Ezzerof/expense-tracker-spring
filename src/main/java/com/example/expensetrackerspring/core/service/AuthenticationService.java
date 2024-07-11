package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.SignInRequest;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;
import com.example.expensetrackerspring.rest.payload.response.SignInResponse;

public interface AuthenticationService {
    void userSignUp(SignUpRequest signUpRequest);
    SignInResponse userSignIn(SignInRequest signInRequest);
}
