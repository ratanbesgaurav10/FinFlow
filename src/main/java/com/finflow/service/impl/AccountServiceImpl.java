package com.finflow.service.impl;

import com.finflow.dto.request.AccountRequest;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.exception.AccountException;
import com.finflow.exception.ResourceNotFoundException;
import com.finflow.model.Account;
import com.finflow.model.User;
import com.finflow.repository.AccountRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import com.finflow.service.AccountService;
import com.finflow.util.AccountNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository, AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }


    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Override
    @Transactional
    public ResponseDTOs.AccountResponse createAccount(AccountRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);

        long existingAccounts = accountRepository.countByUserId(user.getId());
        if (existingAccounts >= 3) {
            throw new AccountException("Maximum of 3 accounts allowed per user");
        }

        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {} for user: {}", saved.getAccountNumber(), userEmail);
        return mapToAccountResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTOs.AccountResponse getAccountByNumber(String accountNumber, String userEmail) {
        Account account = findAccountByNumber(accountNumber);
        validateAccountOwnership(account, userEmail);
        return mapToAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseDTOs.AccountResponse> getUserAccounts(String userEmail) {
        User user = findUserByEmail(userEmail);
        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseDTOs.AccountResponse freezeAccount(String accountNumber, String adminEmail) {
        Account account = findAccountByNumber(accountNumber);
        account.setStatus(Account.AccountStatus.FROZEN);
        return mapToAccountResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public ResponseDTOs.AccountResponse closeAccount(String accountNumber, String userEmail) {
        Account account = findAccountByNumber(accountNumber);
        validateAccountOwnership(account, userEmail);

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new AccountException("Cannot close account with remaining balance. Please withdraw funds first.");
        }

        account.setStatus(Account.AccountStatus.CLOSED);
        return mapToAccountResponse(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTOs.AccountSummary getAccountSummary(String accountNumber, String userEmail, int days) {
        Account account = findAccountByNumber(accountNumber);
        validateAccountOwnership(account, userEmail);

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days);

        BigDecimal totalCredits = transactionRepository
                .sumCreditsByAccountIdAndDateRange(account.getId(), from, to);
        BigDecimal totalDebits = transactionRepository
                .sumDebitsByAccountIdAndDateRange(account.getId(), from, to);

        return ResponseDTOs.AccountSummary.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .totalCredits(totalCredits != null ? totalCredits : BigDecimal.ZERO)
                .totalDebits(totalDebits != null ? totalDebits : BigDecimal.ZERO)
                .from(from)
                .to(to)
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "number", accountNumber));
    }

    private void validateAccountOwnership(Account account, String userEmail) {
        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new AccountException("Access denied: you do not own this account");
        }
    }

    public ResponseDTOs.AccountResponse mapToAccountResponse(Account account) {
        return ResponseDTOs.AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().name())
                .userId(account.getUser().getId())
                .ownerName(account.getUser().getFullName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
