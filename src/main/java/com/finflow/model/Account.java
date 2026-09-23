package com.finflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "account_number", nullable = false, unique = true) private String accountNumber;
    @Enumerated(EnumType.STRING) @Column(name = "account_type", nullable = false) private AccountType accountType;
    @Column(name = "balance", nullable = false, precision = 15, scale = 2) private BigDecimal balance=BigDecimal.ZERO;
    @Column(name = "currency", nullable = false, length = 3) private String currency="INR";
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private AccountStatus status=AccountStatus.ACTIVE;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private User user;
    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private List<Transaction> debitTransactions=new ArrayList<>();
    @OneToMany(mappedBy = "destinationAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private List<Transaction> creditTransactions=new ArrayList<>();
    public Account() {}
    public Account(Long id,String accountNumber,AccountType accountType,BigDecimal balance,String currency,AccountStatus status,User user,List<Transaction> debitTransactions,List<Transaction> creditTransactions){this.id=id;this.accountNumber=accountNumber;this.accountType=accountType;this.balance=balance;this.currency=currency;this.status=status;this.user=user;this.debitTransactions=debitTransactions;this.creditTransactions=creditTransactions;}
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){accountNumber=v;} public AccountType getAccountType(){return accountType;} public void setAccountType(AccountType v){accountType=v;} public BigDecimal getBalance(){return balance;} public void setBalance(BigDecimal v){balance=v;} public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;} public AccountStatus getStatus(){return status;} public void setStatus(AccountStatus v){status=v;} public User getUser(){return user;} public void setUser(User v){user=v;} public List<Transaction> getDebitTransactions(){return debitTransactions;} public void setDebitTransactions(List<Transaction> v){debitTransactions=v;} public List<Transaction> getCreditTransactions(){return creditTransactions;} public void setCreditTransactions(List<Transaction> v){creditTransactions=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder { private Long id; private String accountNumber; private AccountType accountType; private BigDecimal balance=BigDecimal.ZERO; private String currency="INR"; private AccountStatus status=AccountStatus.ACTIVE; private User user; private List<Transaction> debitTransactions=new ArrayList<>(), creditTransactions=new ArrayList<>(); public Builder id(Long v){id=v;return this;} public Builder accountNumber(String v){accountNumber=v;return this;} public Builder accountType(AccountType v){accountType=v;return this;} public Builder balance(BigDecimal v){balance=v;return this;} public Builder currency(String v){currency=v;return this;} public Builder status(AccountStatus v){status=v;return this;} public Builder user(User v){user=v;return this;} public Builder debitTransactions(List<Transaction> v){debitTransactions=v;return this;} public Builder creditTransactions(List<Transaction> v){creditTransactions=v;return this;} public Account build(){return new Account(id,accountNumber,accountType,balance,currency,status,user,debitTransactions,creditTransactions);} }
    public enum AccountType { SAVINGS, CURRENT, WALLET }
    public enum AccountStatus { ACTIVE, INACTIVE, FROZEN, CLOSED }
}
