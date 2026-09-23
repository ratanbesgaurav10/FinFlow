package com.finflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_reference", columnList = "reference_number"),
    @Index(name = "idx_txn_source_account", columnList = "source_account_id"),
    @Index(name = "idx_txn_destination_account", columnList = "destination_account_id"),
    @Index(name = "idx_txn_status", columnList = "status"),
    @Index(name = "idx_txn_created_at", columnList = "created_at")
})
public class Transaction extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "reference_number", nullable = false, unique = true) private String referenceNumber;
    @Enumerated(EnumType.STRING) @Column(name = "transaction_type", nullable = false) private TransactionType transactionType;
    @Column(name = "amount", nullable = false, precision = 15, scale = 2) private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) private String currency="INR";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_account_id") private Account sourceAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "destination_account_id") private Account destinationAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sender_id") private User sender;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id") private User recipient;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private TransactionStatus status=TransactionStatus.PENDING;
    @Column(name = "description") private String description;
    @Column(name = "remarks") private String remarks;
    @Column(name = "processed_at") private LocalDateTime processedAt;
    @Column(name = "failure_reason") private String failureReason;
    public Transaction() {}
    public Transaction(Long id,String referenceNumber,TransactionType transactionType,BigDecimal amount,String currency,Account sourceAccount,Account destinationAccount,User sender,User recipient,TransactionStatus status,String description,String remarks,LocalDateTime processedAt,String failureReason){this.id=id;this.referenceNumber=referenceNumber;this.transactionType=transactionType;this.amount=amount;this.currency=currency;this.sourceAccount=sourceAccount;this.destinationAccount=destinationAccount;this.sender=sender;this.recipient=recipient;this.status=status;this.description=description;this.remarks=remarks;this.processedAt=processedAt;this.failureReason=failureReason;}
    public Long getId(){return id;} public void setId(Long v){id=v;} public String getReferenceNumber(){return referenceNumber;} public void setReferenceNumber(String v){referenceNumber=v;} public TransactionType getTransactionType(){return transactionType;} public void setTransactionType(TransactionType v){transactionType=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public String getCurrency(){return currency;} public void setCurrency(String v){currency=v;} public Account getSourceAccount(){return sourceAccount;} public void setSourceAccount(Account v){sourceAccount=v;} public Account getDestinationAccount(){return destinationAccount;} public void setDestinationAccount(Account v){destinationAccount=v;} public User getSender(){return sender;} public void setSender(User v){sender=v;} public User getRecipient(){return recipient;} public void setRecipient(User v){recipient=v;} public TransactionStatus getStatus(){return status;} public void setStatus(TransactionStatus v){status=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;} public LocalDateTime getProcessedAt(){return processedAt;} public void setProcessedAt(LocalDateTime v){processedAt=v;} public String getFailureReason(){return failureReason;} public void setFailureReason(String v){failureReason=v;}
    public static Builder builder(){return new Builder();}
    public static class Builder { private Long id; private String referenceNumber; private TransactionType transactionType; private BigDecimal amount; private String currency="INR"; private Account sourceAccount,destinationAccount; private User sender,recipient; private TransactionStatus status=TransactionStatus.PENDING; private String description,remarks; private LocalDateTime processedAt; private String failureReason; public Builder id(Long v){id=v;return this;} public Builder referenceNumber(String v){referenceNumber=v;return this;} public Builder transactionType(TransactionType v){transactionType=v;return this;} public Builder amount(BigDecimal v){amount=v;return this;} public Builder currency(String v){currency=v;return this;} public Builder sourceAccount(Account v){sourceAccount=v;return this;} public Builder destinationAccount(Account v){destinationAccount=v;return this;} public Builder sender(User v){sender=v;return this;} public Builder recipient(User v){recipient=v;return this;} public Builder status(TransactionStatus v){status=v;return this;} public Builder description(String v){description=v;return this;} public Builder remarks(String v){remarks=v;return this;} public Builder processedAt(LocalDateTime v){processedAt=v;return this;} public Builder failureReason(String v){failureReason=v;return this;} public Transaction build(){return new Transaction(id,referenceNumber,transactionType,amount,currency,sourceAccount,destinationAccount,sender,recipient,status,description,remarks,processedAt,failureReason);} }
    public enum TransactionType { TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT, REFUND }
    public enum TransactionStatus { PENDING, PROCESSING, COMPLETED, FAILED, REVERSED }
}
