package com.finflow.service;

import com.finflow.dto.request.TransactionRequest;
import com.finflow.dto.response.ResponseDTOs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    ResponseDTOs.TransactionResponse transfer(TransactionRequest.Transfer request, String userEmail);
    ResponseDTOs.TransactionResponse deposit(TransactionRequest.Deposit request, String userEmail);
    ResponseDTOs.TransactionResponse withdraw(TransactionRequest.Withdrawal request, String userEmail);
    ResponseDTOs.TransactionResponse getTransactionByReference(String referenceNumber, String userEmail);
    Page<ResponseDTOs.TransactionResponse> getUserTransactions(String userEmail, Pageable pageable);
    Page<ResponseDTOs.TransactionResponse> getAccountTransactions(String accountNumber, String userEmail, Pageable pageable);
}
