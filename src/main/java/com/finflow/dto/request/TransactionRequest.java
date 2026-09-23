package com.finflow.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class TransactionRequest {
    public static class Transfer {
        @NotBlank(message = "Source account number is required") private String sourceAccountNumber;
        @NotBlank(message = "Destination account number is required") private String destinationAccountNumber;
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than 0") @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 10,00,000") private BigDecimal amount;
        @Size(max = 255, message = "Description cannot exceed 255 characters") private String description;
        public String getSourceAccountNumber(){return sourceAccountNumber;} public void setSourceAccountNumber(String v){sourceAccountNumber=v;}
        public String getDestinationAccountNumber(){return destinationAccountNumber;} public void setDestinationAccountNumber(String v){destinationAccountNumber=v;}
        public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
        public String getDescription(){return description;} public void setDescription(String v){description=v;}
    }
    public static class Deposit {
        @NotBlank(message = "Account number is required") private String accountNumber;
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than 0") private BigDecimal amount;
        @Size(max = 255) private String description;
        public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){accountNumber=v;}
        public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
        public String getDescription(){return description;} public void setDescription(String v){description=v;}
    }
    public static class Withdrawal {
        @NotBlank(message = "Account number is required") private String accountNumber;
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than 0") private BigDecimal amount;
        @Size(max = 255) private String description;
        public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){accountNumber=v;}
        public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
        public String getDescription(){return description;} public void setDescription(String v){description=v;}
    }
}
