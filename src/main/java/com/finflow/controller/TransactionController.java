package com.finflow.controller;

import com.finflow.dto.request.TransactionRequest;
import com.finflow.dto.response.ApiResponse;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction processing endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    public ResponseEntity<ApiResponse<ResponseDTOs.TransactionResponse>> transfer(
            @Valid @RequestBody TransactionRequest.Transfer request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.TransactionResponse response = transactionService.transfer(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into an account")
    public ResponseEntity<ApiResponse<ResponseDTOs.TransactionResponse>> deposit(
            @Valid @RequestBody TransactionRequest.Deposit request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.TransactionResponse response = transactionService.deposit(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit completed successfully", response));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    public ResponseEntity<ApiResponse<ResponseDTOs.TransactionResponse>> withdraw(
            @Valid @RequestBody TransactionRequest.Withdrawal request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.TransactionResponse response = transactionService.withdraw(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal completed successfully", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all transactions for logged-in user")
    public ResponseEntity<ApiResponse<Page<ResponseDTOs.TransactionResponse>>> getMyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ResponseDTOs.TransactionResponse> transactions =
                transactionService.getUserTransactions(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", transactions));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transactions for a specific account")
    public ResponseEntity<ApiResponse<Page<ResponseDTOs.TransactionResponse>>> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ResponseDTOs.TransactionResponse> transactions =
                transactionService.getAccountTransactions(accountNumber, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Account transactions retrieved", transactions));
    }

    @GetMapping("/{referenceNumber}")
    @Operation(summary = "Get transaction by reference number")
    public ResponseEntity<ApiResponse<ResponseDTOs.TransactionResponse>> getTransaction(
            @PathVariable String referenceNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.TransactionResponse response =
                transactionService.getTransactionByReference(referenceNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved", response));
    }
}
