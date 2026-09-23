package com.finflow.controller;

import com.finflow.dto.response.ApiResponse;
import com.finflow.dto.response.ResponseDTOs;
import com.finflow.model.User;
import com.finflow.repository.TransactionRepository;
import com.finflow.repository.UserRepository;
import com.finflow.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {
    public AdminController(UserRepository userRepository, TransactionRepository transactionRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }


    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get system dashboard statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        stats.put("completedTransactions", transactionRepository.countByStatus(
                com.finflow.model.Transaction.TransactionStatus.COMPLETED));
        stats.put("pendingTransactions", transactionRepository.countByStatus(
                com.finflow.model.Transaction.TransactionStatus.PENDING));
        stats.put("failedTransactions", transactionRepository.countByStatus(
                com.finflow.model.Transaction.TransactionStatus.FAILED));

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<ResponseDTOs.UserResponse>>> getAllUsers() {
        List<ResponseDTOs.UserResponse> users = userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @PatchMapping("/accounts/{accountNumber}/freeze")
    @Operation(summary = "Freeze an account")
    public ResponseEntity<ApiResponse<ResponseDTOs.AccountResponse>> freezeAccount(
            @PathVariable String accountNumber) {
        ResponseDTOs.AccountResponse response = accountService.freezeAccount(accountNumber, "admin");
        return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", response));
    }

    private ResponseDTOs.UserResponse mapToUserResponse(User user) {
        return ResponseDTOs.UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
