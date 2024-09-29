package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.exceptions.*;
import com.example.expensetrackerspring.core.persistance.entity.Expense;
import com.example.expensetrackerspring.core.persistance.entity.Income;
import com.example.expensetrackerspring.core.persistance.entity.User;
import com.example.expensetrackerspring.core.persistance.repository.ExpenseRepository;
import com.example.expensetrackerspring.core.persistance.repository.IncomeRepository;
import com.example.expensetrackerspring.core.persistance.repository.UserRepository;
import com.example.expensetrackerspring.rest.payload.request.SaveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.GetTransactionRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveTransactionRequest;
import com.example.expensetrackerspring.rest.payload.response.TransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveTransactionResponse;
import com.example.expensetrackerspring.rest.payload.response.SaveTransactionResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class TransactionServiceImpl implements TransactionService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(ExpenseRepository expenseRepository, IncomeRepository incomeRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
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

        if (transactionExists(saveTransactionRequest.name(), saveTransactionRequest.transactionType())) {
            throw new DuplicateTransactionException("Transaction already exists");
        }

        if (saveTransactionRequest.transactionType() == TransactionType.EXPENSE) {
            expenseRepository.save(Expense.builder()
                    .user(user)
                    .name(saveTransactionRequest.name())
                    .description(saveTransactionRequest.description())
                    .amount(saveTransactionRequest.amount())
                    .category(saveTransactionRequest.category())
                    .date(saveTransactionRequest.date())
                    .build());

            return new SaveTransactionResponse(true, "Expense saved");
        } else if (saveTransactionRequest.transactionType() == TransactionType.INCOME) {
            incomeRepository.save(Income.builder()
                    .user(user)
                    .name(saveTransactionRequest.name())
                    .description(saveTransactionRequest.description())
                    .amount(saveTransactionRequest.amount())
                    .category(saveTransactionRequest.category())
                    .date(saveTransactionRequest.date())
                    .build());

            return new SaveTransactionResponse(true, "Income saved");
        } else {
            throw new IllegalArgumentException("Unknown transaction type");
        }
    }

    @Override
    public Optional<TransactionResponse> getTransaction(GetTransactionRequest getTransactionRequest, Long userId) {
        if (getTransactionRequest.transactionType() == TransactionType.EXPENSE) {
            return expenseRepository.findByIdAndUserId(getTransactionRequest.id(), userId)
                    .map(expense -> new TransactionResponse(
                            expense.getId(),
                            expense.getName(),
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getCategory(),
                            expense.getDate()
                    ));
        } else if (getTransactionRequest.transactionType() == TransactionType.INCOME) {
            return incomeRepository.findByIdAndUserId(getTransactionRequest.id(), userId)
                    .map(income -> new TransactionResponse(
                            income.getId(),
                            income.getName(),
                            income.getDescription(),
                            income.getAmount(),
                            income.getCategory(),
                            income.getDate()
                    ));
        } else {
            throw new IllegalArgumentException("Unknown transaction type: " + getTransactionRequest.transactionType());
        }
    }


    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable, Long userId, TransactionType transactionType) {
        if (transactionType == TransactionType.EXPENSE) {
            return expenseRepository.findByUserId(userId, pageable)
                    .map(expense -> new TransactionResponse(
                            expense.getId(),
                            expense.getName(),
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getCategory(),
                            expense.getDate()
                    ));
        } else if (transactionType == TransactionType.INCOME) {
            return incomeRepository.findByUserId(userId, pageable)
                    .map(income -> new TransactionResponse(
                            income.getId(),
                            income.getName(),
                            income.getDescription(),
                            income.getAmount(),
                            income.getCategory(),
                            income.getDate()
                    ));
        } else {
            throw new IllegalArgumentException("Unknown transaction type: " + transactionType);
        }
    }
    @Transactional
    @Override
    public Optional<TransactionResponse> updateTransaction(SaveTransactionRequest saveTransactionRequest, Long userId) {
        if (saveTransactionRequest == null || saveTransactionRequest.name() == null || saveTransactionRequest.amount() == null) {
            throw new InvalidTransactionDetailsException("Invalid transaction details");
        }

        if (saveTransactionRequest.transactionType() == TransactionType.EXPENSE) {
            Expense expense = expenseRepository.findByIdAndUserId(saveTransactionRequest.id(), userId)
                    .orElseThrow(() -> new TransactionNotFoundException("Expense not found or access denied"));

            expense.setName(saveTransactionRequest.name());
            expense.setDate(saveTransactionRequest.date());
            expense.setCategory(saveTransactionRequest.category());
            expense.setDescription(saveTransactionRequest.description());
            expense.setAmount(saveTransactionRequest.amount());
            expenseRepository.save(expense);

            return Optional.of(convertExpenseToDto(expense));
        } else if (saveTransactionRequest.transactionType() == TransactionType.INCOME) {
            Income income = incomeRepository.findByIdAndUserId(saveTransactionRequest.id(), userId)
                    .orElseThrow(() -> new TransactionNotFoundException("Income not found or access denied"));

            income.setName(saveTransactionRequest.name());
            income.setDate(saveTransactionRequest.date());
            income.setCategory(saveTransactionRequest.category());
            income.setDescription(saveTransactionRequest.description());
            income.setAmount(saveTransactionRequest.amount());
            incomeRepository.save(income);

            return Optional.of(convertIncomeToDto(income));
        } else {
            throw new IllegalArgumentException("Unknown transaction type: " + saveTransactionRequest.transactionType());
        }
    }


    @Override
    public RemoveTransactionResponse deleteTransaction(RemoveTransactionRequest request, Long userId) {
        if (request.transactionType() == TransactionType.EXPENSE) {
            Expense expense = expenseRepository.findByIdAndUserId(request.id(), userId)
                    .orElseThrow(() -> new TransactionNotFoundException("Expense not found or access denied"));

            expenseRepository.delete(expense);
        } else if (request.transactionType() == TransactionType.INCOME) {
            Income income = incomeRepository.findByIdAndUserId(request.id(), userId)
                    .orElseThrow(() -> new TransactionNotFoundException("Income not found or access denied"));

            incomeRepository.delete(income);
        } else {
            throw new IllegalArgumentException("Unknown transaction type: " + request.transactionType());
        }

        return new RemoveTransactionResponse(true, "Transaction deleted successfully");
    }



    private boolean transactionExists(String transactionName, TransactionType transactionType) {
        if (transactionType == TransactionType.EXPENSE) {
            return expenseRepository.findByName(transactionName).isPresent();
        } else if (transactionType == TransactionType.INCOME) {
            return incomeRepository.findByName(transactionName).isPresent();
        } else {
            throw new IllegalArgumentException("Unknown transaction type: " + transactionType);
        }

    }

    private TransactionResponse convertExpenseToDto(Expense expense) {
        return new TransactionResponse(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }

    private TransactionResponse convertIncomeToDto(Income income) {
        return new TransactionResponse(
                income.getId(),
                income.getName(),
                income.getDescription(),
                income.getAmount(),
                income.getCategory(),
                income.getDate()
        );
    }
}
