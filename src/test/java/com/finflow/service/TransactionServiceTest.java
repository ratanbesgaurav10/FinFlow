package com.finflow.service;

import com.finflow.dto.request.TransactionRequest;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.exception.InsufficientFundsException;
import com.finflow.model.Account;
import com.finflow.model.Transaction;
import com.finflow.model.User;
import com.finflow.repository.AccountRepository;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import com.finflow.service.impl.TransactionServiceImpl;
import com.finflow.util.ReferenceNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReferenceNumberGenerator referenceNumberGenerator;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User alice;
    private User bob;
    private Account aliceAccount;
    private Account bobAccount;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).fullName("Alice").email("alice@finflow.com").build();
        bob = User.builder().id(2L).fullName("Bob").email("bob@finflow.com").build();

        aliceAccount = Account.builder()
                .id(1L).accountNumber("FF24011001").accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("10000.00")).currency("INR")
                .status(Account.AccountStatus.ACTIVE).user(alice).build();

        bobAccount = Account.builder()
                .id(2L).accountNumber("FF24011002").accountType(Account.AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00")).currency("INR")
                .status(Account.AccountStatus.ACTIVE).user(bob).build();
    }

    @Test
    @DisplayName("Should successfully transfer funds between two accounts")
    void transfer_ShouldSucceed_WhenSufficientBalance() {
        TransactionRequest.Transfer request = new TransactionRequest.Transfer();
        request.setSourceAccountNumber("FF24011001");
        request.setDestinationAccountNumber("FF24011002");
        request.setAmount(new BigDecimal("2000.00"));
        request.setDescription("Test transfer");

        when(userRepository.findByEmail("alice@finflow.com")).thenReturn(Optional.of(alice));
        when(accountRepository.findByAccountNumber("FF24011001")).thenReturn(Optional.of(aliceAccount));
        when(accountRepository.findByAccountNumber("FF24011002")).thenReturn(Optional.of(bobAccount));
        when(referenceNumberGenerator.generate()).thenReturn("TXN20240101000001AA");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseDTOs.TransactionResponse response = transactionService.transfer(request, "alice@finflow.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(aliceAccount.getBalance()).isEqualByComparingTo("8000.00");
        assertThat(bobAccount.getBalance()).isEqualByComparingTo("7000.00");
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when balance is too low")
    void transfer_ShouldThrow_WhenInsufficientFunds() {
        TransactionRequest.Transfer request = new TransactionRequest.Transfer();
        request.setSourceAccountNumber("FF24011001");
        request.setDestinationAccountNumber("FF24011002");
        request.setAmount(new BigDecimal("99999.00"));

        when(userRepository.findByEmail("alice@finflow.com")).thenReturn(Optional.of(alice));
        when(accountRepository.findByAccountNumber("FF24011001")).thenReturn(Optional.of(aliceAccount));
        when(accountRepository.findByAccountNumber("FF24011002")).thenReturn(Optional.of(bobAccount));

        assertThatThrownBy(() -> transactionService.transfer(request, "alice@finflow.com"))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully deposit into an account")
    void deposit_ShouldIncreaseBalance() {
        TransactionRequest.Deposit request = new TransactionRequest.Deposit();
        request.setAccountNumber("FF24011001");
        request.setAmount(new BigDecimal("1000.00"));

        when(userRepository.findByEmail("alice@finflow.com")).thenReturn(Optional.of(alice));
        when(accountRepository.findByAccountNumber("FF24011001")).thenReturn(Optional.of(aliceAccount));
        when(referenceNumberGenerator.generate()).thenReturn("TXN20240101000002BB");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseDTOs.TransactionResponse response = transactionService.deposit(request, "alice@finflow.com");

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(aliceAccount.getBalance()).isEqualByComparingTo("11000.00");
    }

    @Test
    @DisplayName("Should successfully withdraw from an account")
    void withdraw_ShouldDecreaseBalance() {
        TransactionRequest.Withdrawal request = new TransactionRequest.Withdrawal();
        request.setAccountNumber("FF24011001");
        request.setAmount(new BigDecimal("500.00"));

        when(userRepository.findByEmail("alice@finflow.com")).thenReturn(Optional.of(alice));
        when(accountRepository.findByAccountNumber("FF24011001")).thenReturn(Optional.of(aliceAccount));
        when(referenceNumberGenerator.generate()).thenReturn("TXN20240101000003CC");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.withdraw(request, "alice@finflow.com");

        assertThat(aliceAccount.getBalance()).isEqualByComparingTo("9500.00");
    }
}
