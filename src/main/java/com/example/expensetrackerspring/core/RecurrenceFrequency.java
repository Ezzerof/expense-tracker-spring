package com.example.expensetrackerspring.core;

public enum RecurrenceFrequency {
    SINGLE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static RecurrenceFrequency fromString(String frequency) {
        return RecurrenceFrequency.valueOf(frequency.toUpperCase());
    }
}
