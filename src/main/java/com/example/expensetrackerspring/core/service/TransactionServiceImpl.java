package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.exceptions.DuplicateTransactionException;
import com.example.expensetrackerspring.core.exceptions.InvalidTransactionDetailsException;
import com.example.expensetrackerspring.core.exceptions.TransactionNotFoundException;
import com.example.expensetrackerspring.core.exceptions.UserNotFoundException;
import com.example.expensetrackerspring.core.persistance.entity.Transaction;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.TransactionRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private static final int DEFAULT_RECURRING_LIMIT = 12;

    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public SaveTransactionResponse saveTransaction(SaveTransactionRequest saveTransactionRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (saveTransactionRequest.name() == null || saveTransactionRequest.amount() == null) {
            throw new InvalidTransactionDetailsException("Invalid transaction details");
        }

        if (transactionExists(saveTransactionRequest.name(), saveTransactionRequest.transactionType(), userId)) {
            throw new DuplicateTransactionException("Transaction already exists");
        }

        handleRecurringTransactions(user, saveTransactionRequest);
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
        if (saveTransactionRequest == null || saveTransactionRequest.name() == null || saveTransactionRequest.amount() == null) {
            throw new InvalidTransactionDetailsException("Invalid transaction details");
        }

        Transaction transaction = transactionRepository.findByIdAndUserId(saveTransactionRequest.id(), userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));

        transaction.setName(saveTransactionRequest.name());
        transaction.setStartDate(saveTransactionRequest.startDate());
        transaction.setEndDate(saveTransactionRequest.endDate());
        transaction.setCategory(saveTransactionRequest.category());
        transaction.setDescription(saveTransactionRequest.description());
        transaction.setAmount(saveTransactionRequest.amount());
        transactionRepository.save(transaction);

        return Optional.of(convertTransactionToDto(transaction));
    }

    @Override
    public RemoveTransactionResponse deleteTransaction(RemoveTransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(request.id(), userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found or access denied"));

        transactionRepository.delete(transaction);
        return new RemoveTransactionResponse(true, "Transaction deleted successfully");
    }

    @Override
    public LocalDate getNextOccurrenceDate(LocalDate currentDate, RecurrenceFrequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
            default -> throw new IllegalArgumentException("Unknown frequency: " + frequency);
        };
    }

    private boolean transactionExists(String transactionName, TransactionType transactionType, Long userId) {
        return transactionRepository.findByNameAndTransactionTypeAndUserId(transactionName, transactionType, userId).isPresent();
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

    private void handleRecurringTransactions(User user, SaveTransactionRequest saveTransactionRequest) {
        LocalDate todayDate = LocalDate.now();
        LocalDate nextDate = saveTransactionRequest.startDate();
        int occurrences = 0;

        if (saveTransactionRequest.recurrenceFrequency() != RecurrenceFrequency.SINGLE) {
            while (nextDate.isAfter(todayDate.minusYears(1)) &&
                    (saveTransactionRequest.endDate() == null || nextDate.isBefore(saveTransactionRequest.endDate())) &&
                    occurrences < DEFAULT_RECURRING_LIMIT) {

                Transaction recurringTransaction = Transaction.builder()
                        .user(user)
                        .name(saveTransactionRequest.name())
                        .description(saveTransactionRequest.description())
                        .category(saveTransactionRequest.category())
                        .amount(saveTransactionRequest.amount())
                        .recurrenceFrequency(saveTransactionRequest.recurrenceFrequency())
                        .startDate(nextDate)
                        .endDate(saveTransactionRequest.endDate())
                        .transactionType(saveTransactionRequest.transactionType())
                        .build();

                transactionRepository.save(recurringTransaction);
                nextDate = getNextOccurrenceDate(nextDate, saveTransactionRequest.recurrenceFrequency());
                occurrences++;
            }
        }

        transactionRepository.save(Transaction.builder()
                .user(user)
                .name(saveTransactionRequest.name())
                .description(saveTransactionRequest.description())
                .amount(saveTransactionRequest.amount())
                .category(saveTransactionRequest.category())
                .recurrenceFrequency(saveTransactionRequest.recurrenceFrequency())
                .startDate(saveTransactionRequest.startDate())
                .endDate(saveTransactionRequest.endDate())
                .transactionType(saveTransactionRequest.transactionType())
                .build());
    }

    @Override
    public List<TransactionResponse> getTransactionsByDate(LocalDate date, Long userId) {
        List<Transaction> transactions = transactionRepository.findByStartDateAndUserId(date, userId);

        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionResponses.add(convertTransactionToDto(transaction));
        }
        return transactionResponses;
    }

    public List<TransactionResponse> getTransactionsForMonth(Long userId, String yearMonth) {
        YearMonth month = YearMonth.parse(yearMonth);
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findAllByUserIdAndStartDateBetween(userId, startDate, endDate);

        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for (Transaction transaction : transactions) {
            transactionResponses.add(convertTransactionToDto(transaction));
        }

        transactionResponses.sort(Comparator.comparing(TransactionResponse::startDate));
        return transactionResponses;
    }
}
