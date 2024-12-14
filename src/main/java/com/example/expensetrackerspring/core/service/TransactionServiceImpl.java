package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.exceptions.TransactionNotFoundException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.TransactionRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserMonthlySummaryRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.RemoveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserMonthlySummaryRepository userMonthlySummaryRepository;
    private final UserMonthlySummaryService userMonthlySummaryService;

    private static final int DEFAULT_RECURRING_LIMIT = 12;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  UserMonthlySummaryRepository userMonthlySummaryRepository,
                                  UserMonthlySummaryService userMonthlySummaryService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.userMonthlySummaryRepository = userMonthlySummaryRepository;
        this.userMonthlySummaryService = userMonthlySummaryService;
    }

    @Override
    @Transactional
    public SaveTransactionResponse saveTransaction(SaveTransactionRequest saveTransactionRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        LocalDate startDate = saveTransactionRequest.startDate();

        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        LocalDate endDate = saveTransactionRequest.endDate() != null ? saveTransactionRequest.endDate() : startDate;

        RecurrenceFrequency frequency;
        try {
            frequency = saveTransactionRequest.recurrenceFrequency();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid frequency: " + saveTransactionRequest.recurrenceFrequency());
        }

        if (frequency != RecurrenceFrequency.SINGLE) {
            if (endDate == null) {
                throw new IllegalArgumentException("End date cannot be null for recurring transactions");
            }

            int occurrences = 0;
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate) && occurrences < DEFAULT_RECURRING_LIMIT) {
                Transaction transaction = Transaction.builder()
                        .user(user)
                        .name(saveTransactionRequest.name())
                        .description(saveTransactionRequest.description())
                        .amount(saveTransactionRequest.amount())
                        .category(saveTransactionRequest.category())
                        .recurrenceFrequency(frequency)
                        .startDate(currentDate)
                        .endDate(endDate)
                        .transactionType(saveTransactionRequest.transactionType())
                        .build();

                transactionRepository.save(transaction);

                userMonthlySummaryService.updateDailySummary(currentDate, user);

                currentDate = getNextOccurrenceDate(currentDate, frequency);
                occurrences++;
            }
        } else {
            Transaction transaction = Transaction.builder()
                    .user(user)
                    .name(saveTransactionRequest.name())
                    .description(saveTransactionRequest.description())
                    .amount(saveTransactionRequest.amount())
                    .category(saveTransactionRequest.category())
                    .recurrenceFrequency(frequency)
                    .startDate(startDate)
                    .endDate(startDate)
                    .transactionType(saveTransactionRequest.transactionType())
                    .build();

            transactionRepository.save(transaction);

            userMonthlySummaryService.updateDailySummary(startDate, user);
        }

        return new SaveTransactionResponse(true, "Transaction saved successfully");
    }




    @Override
    public Optional<TransactionResponse> getTransaction(GetTransactionRequest getTransactionRequest, Long userId) {
        return transactionRepository.findByIdAndUserId(getTransactionRequest.id(), userId)
                .map(this::convertTransactionToDto);
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable, Long userId, TransactionType transactionType) {
        return transactionRepository.findByUserIdAndTransactionType(userId, transactionType, pageable)
                .map(this::convertTransactionToDto);
    }

    @Transactional
    @Override
    public Optional<TransactionResponse> updateTransaction(SaveTransactionRequest saveTransactionRequest, Long userId) {
        Transaction oldTransaction = transactionRepository.findByIdAndUserId(saveTransactionRequest.id(), userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));

        List<LocalDate> affectedDates = calculateAffectedDates(oldTransaction);
        for (LocalDate date : affectedDates) {
            userMonthlySummaryService.updateDailySummary(date, oldTransaction.getUser());
        }

        oldTransaction.setName(saveTransactionRequest.name());
        oldTransaction.setStartDate(saveTransactionRequest.startDate());
        oldTransaction.setEndDate(saveTransactionRequest.endDate());
        oldTransaction.setCategory(saveTransactionRequest.category());
        oldTransaction.setDescription(saveTransactionRequest.description());
        oldTransaction.setAmount(saveTransactionRequest.amount());
        oldTransaction.setTransactionType(saveTransactionRequest.transactionType());
        oldTransaction.setRecurrenceFrequency(saveTransactionRequest.recurrenceFrequency());
        transactionRepository.save(oldTransaction);

        affectedDates = calculateAffectedDates(oldTransaction);
        for (LocalDate date : affectedDates) {
            userMonthlySummaryService.updateDailySummary(date, oldTransaction.getUser());
        }

        return Optional.of(convertTransactionToDto(oldTransaction));
    }


    @Override
    @Transactional
    public RemoveTransactionResponse deleteTransaction(RemoveTransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(request.id(), userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));
        List<LocalDate> affectedDates = calculateAffectedDates(transaction);

        transactionRepository.delete(transaction);

        for (LocalDate date : affectedDates) {
            userMonthlySummaryService.updateDailySummary(date, transaction.getUser());
        }

        return new RemoveTransactionResponse(true, "Transaction deleted successfully");
    }

    @Transactional
    public void deleteAllOccurrences(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));

        List<Transaction> allOccurrences = transactionRepository.findByUserAndName(transaction.getUser(), transaction.getName());

        allOccurrences.forEach(t -> {
            List<LocalDate> affectedDates = calculateAffectedDates(t);
            transactionRepository.delete(t);

            affectedDates.forEach(date -> userMonthlySummaryService.updateDailySummary(date, t.getUser()));
        });
    }


    private TransactionResponse convertTransactionToDto(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getName(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getStartDate(),
                transaction.getEndDate(),
                transaction.getTransactionType()
        );
    }

    private List<LocalDate> calculateAffectedDates(Transaction transaction) {
        List<LocalDate> affectedDates = new ArrayList<>();
        LocalDate currentDate = transaction.getStartDate();
        LocalDate endDate = transaction.getEndDate() != null ? transaction.getEndDate() : currentDate;

        if (transaction.getRecurrenceFrequency() == RecurrenceFrequency.SINGLE) {
            affectedDates.add(currentDate);
            return affectedDates;
        }

        while (!currentDate.isAfter(endDate)) {
            affectedDates.add(currentDate);
            currentDate = getNextOccurrenceDate(currentDate, transaction.getRecurrenceFrequency());
        }

        return affectedDates;
    }

    @Override
    public List<TransactionResponse> getTransactionsForMonth(Long userId, String yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        YearMonth month = YearMonth.parse(yearMonth);
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);

        return transactions.stream()
                .map(this::convertTransactionToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsForDay(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUserAndDate(user, date);

        return transactions.stream()
                .map(this::convertTransactionToDto)
                .collect(Collectors.toList());
    }


    @Override
    public LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("RecurrenceFrequency cannot be null");
        }

        return switch (frequency) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
            default -> throw new IllegalArgumentException("Unknown frequency: " + frequency);
        };
    }

}
