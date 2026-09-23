package com.finflow.service;

import com.finflow.dto.request.AccountRequest;
import com.finflow.dto.response.ResponseDTOs;

import java.util.List;

public interface AccountService {
    ResponseDTOs.AccountResponse createAccount(AccountRequest request, String userEmail);
    ResponseDTOs.AccountResponse getAccountByNumber(String accountNumber, String userEmail);
    List<ResponseDTOs.AccountResponse> getUserAccounts(String userEmail);
    ResponseDTOs.AccountResponse freezeAccount(String accountNumber, String adminEmail);
    ResponseDTOs.AccountResponse closeAccount(String accountNumber, String userEmail);
    ResponseDTOs.AccountSummary getAccountSummary(String accountNumber, String userEmail, int days);
}
