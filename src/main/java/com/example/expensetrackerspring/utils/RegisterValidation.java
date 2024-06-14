package com.example.expensetrackerspring.utils;

import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.rest.payload.request.SignUpRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterValidation {

    public static boolean validateUser(SignUpRequest signUpRequest) {
        return isFirstNameValid(signUpRequest.firstName()) &&
                isEmailValid(signUpRequest.email()) &&
                isUsernameValid(signUpRequest.username());
    }

    private static boolean isFirstNameValid(String firstName) {
        if (firstName.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcher = pattern.matcher(firstName);

        return matcher.matches();
    }

    private static boolean isUsernameValid(String username) {
        if (username.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[a-zA-Z\\d]{5,15}$");
        Matcher matcher = pattern.matcher(username);

        return matcher.matches();
    }

    private static boolean isEmailValid(String email) {
        if (email.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

}
