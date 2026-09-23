package com.finflow.service.impl;

import com.finflow.dto.request.TransactionRequest;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.exception.AccountException;
import com.finflow.exception.InsufficientFundsException;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.model.Account;
import com.finflow.model.Transaction;
import com.finflow.model.User;
import com.finflow.repository.AccountRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import com.finflow.service.TransactionService;
import com.finflow.util.ReferenceNumberGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransactionServiceImpl.class);

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository, UserRepository userRepository, ReferenceNumberGenerator referenceNumberGenerator) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.referenceNumberGenerator = referenceNumberGenerator;
    }


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    @Override
    @Transactional
    public ResponseDTOs.TransactionResponse transfer(TransactionRequest.Transfer request, String userEmail) {
        User sender = findUserByEmail(userEmail);
        Account source = findAndValidateAccount(request.getSourceAccountNumber(), sender);
        Account destination = findAccountByNumber(request.getDestinationAccountNumber());

        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new AccountException("Source and destination accounts cannot be the same");
        }

        validateSufficientBalance(source, request.getAmount());

        // Perform atomic balance update
        source.setBalance(source.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        Transaction transaction = Transaction.builder()
                .referenceNumber(referenceNumberGenerator.generate())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .currency(source.getCurrency())
                .sourceAccount(source)
                .destinationAccount(destination)
                .sender(sender)
                .recipient(destination.getUser())
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .processedAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer completed: {} -> {} | Amount: {} | Ref: {}",
                source.getAccountNumber(), destination.getAccountNumber(),
                request.getAmount(), saved.getReferenceNumber());

        return mapToTransactionResponse(saved);
    }

    @Override
    @Transactional
    public ResponseDTOs.TransactionResponse deposit(TransactionRequest.Deposit request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Account account = findAndValidateAccount(request.getAccountNumber(), user);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(referenceNumberGenerator.generate())
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .destinationAccount(account)
                .recipient(user)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .processedAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit: {} | Amount: {} | Ref: {}",
                account.getAccountNumber(), request.getAmount(), saved.getReferenceNumber());

        return mapToTransactionResponse(saved);
    }

    @Override
    @Transactional
    public ResponseDTOs.TransactionResponse withdraw(TransactionRequest.Withdrawal request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Account account = findAndValidateAccount(request.getAccountNumber(), user);

        validateSufficientBalance(account, request.getAmount());

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .referenceNumber(referenceNumberGenerator.generate())
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .sourceAccount(account)
                .sender(user)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .processedAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal: {} | Amount: {} | Ref: {}",
                account.getAccountNumber(), request.getAmount(), saved.getReferenceNumber());

        return mapToTransactionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTOs.TransactionResponse getTransactionByReference(String referenceNumber, String userEmail) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", referenceNumber));
        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResponseDTOs.TransactionResponse> getUserTransactions(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        return transactionRepository.findTransactionsByUserId(user.getId(), pageable)
                .map(this::mapToTransactionResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResponseDTOs.TransactionResponse> getAccountTransactions(
            String accountNumber, String userEmail, Pageable pageable) {
        Account account = findAccountByNumber(accountNumber);
        return transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(account.getId(), account.getId(), pageable)
                .map(this::mapToTransactionResponse);
    }

    // --- Private helpers ---

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "number", accountNumber));
    }

    private Account findAndValidateAccount(String accountNumber, User owner) {
        Account account = findAccountByNumber(accountNumber);
        if (!account.getUser().getId().equals(owner.getId())) {
            throw new AccountException("Access denied: you do not own account " + accountNumber);
        }
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new AccountException("Account " + accountNumber + " is not active (status: " + account.getStatus() + ")");
        }
        return account;
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available: %.2f, Requested: %.2f",
                            account.getBalance(), amount));
        }
    }

    private ResponseDTOs.TransactionResponse mapToTransactionResponse(Transaction t) {
        return ResponseDTOs.TransactionResponse.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .transactionType(t.getTransactionType().name())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .sourceAccountNumber(t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : null)
                .destinationAccountNumber(t.getDestinationAccount() != null ? t.getDestinationAccount().getAccountNumber() : null)
                .senderName(t.getSender() != null ? t.getSender().getFullName() : null)
                .recipientName(t.getRecipient() != null ? t.getRecipient().getFullName() : null)
                .status(t.getStatus().name())
                .description(t.getDescription())
                .remarks(t.getRemarks())
                .processedAt(t.getProcessedAt())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
