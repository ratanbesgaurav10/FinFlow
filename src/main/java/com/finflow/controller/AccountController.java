package com.finflow.controller;

import com.finflow.dto.request.AccountRequest;
import com.finflow.dto.response.ApiResponse;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<ApiResponse<ResponseDTOs.AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.AccountResponse response = accountService.createAccount(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all accounts for logged-in user")
    public ResponseEntity<ApiResponse<List<ResponseDTOs.AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResponseDTOs.AccountResponse> accounts = accountService.getUserAccounts(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved", accounts));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by account number")
    public ResponseEntity<ApiResponse<ResponseDTOs.AccountResponse>> getAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.AccountResponse response = accountService.getAccountByNumber(accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account retrieved", response));
    }

    @GetMapping("/{accountNumber}/summary")
    @Operation(summary = "Get account transaction summary")
    public ResponseEntity<ApiResponse<ResponseDTOs.AccountSummary>> getAccountSummary(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.AccountSummary summary = accountService.getAccountSummary(
                accountNumber, userDetails.getUsername(), days);
        return ResponseEntity.ok(ApiResponse.success("Account summary retrieved", summary));
    }

    @PatchMapping("/{accountNumber}/close")
    @Operation(summary = "Close an account")
    public ResponseEntity<ApiResponse<ResponseDTOs.AccountResponse>> closeAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResponseDTOs.AccountResponse response = accountService.closeAccount(accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", response));
    }
}
