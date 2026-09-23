package com.finflow.dto.request;

import com.finflow.model.Account;
import jakarta.validation.constraints.NotNull;

public class AccountRequest {
    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
    private String currency = "INR";

    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
