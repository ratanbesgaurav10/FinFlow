package com.finflow.config;

import com.finflow.model.Account;
import com.finflow.model.User;
import com.finflow.repository.AccountRepository;
import com.finflow.repository.UserRepository;
import com.finflow.util.AccountNumberGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder, AccountNumberGenerator accountNumberGenerator) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountNumberGenerator accountNumberGenerator;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) return;

            // Admin user
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@finflow.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .role(User.Role.ADMIN)
                    .build();
            admin = userRepository.save(admin);

            // Demo user 1
            User alice = User.builder()
                    .fullName("Alice Sharma")
                    .email("alice@finflow.com")
                    .password(passwordEncoder.encode("Alice@1234"))
                    .role(User.Role.USER)
                    .build();
            alice = userRepository.save(alice);

            // Demo user 2
            User bob = User.builder()
                    .fullName("Bob Verma")
                    .email("bob@finflow.com")
                    .password(passwordEncoder.encode("Bob@12345"))
                    .role(User.Role.USER)
                    .build();
            bob = userRepository.save(bob);

            // Create accounts
            Account aliceSavings = Account.builder()
                    .accountNumber(accountNumberGenerator.generate())
                    .accountType(Account.AccountType.SAVINGS)
                    .balance(new BigDecimal("50000.00"))
                    .currency("INR")
                    .status(Account.AccountStatus.ACTIVE)
                    .user(alice)
                    .build();
            accountRepository.save(aliceSavings);

            Account bobSavings = Account.builder()
                    .accountNumber(accountNumberGenerator.generate())
                    .accountType(Account.AccountType.SAVINGS)
                    .balance(new BigDecimal("30000.00"))
                    .currency("INR")
                    .status(Account.AccountStatus.ACTIVE)
                    .user(bob)
                    .build();
            accountRepository.save(bobSavings);

            Account bobCurrent = Account.builder()
                    .accountNumber(accountNumberGenerator.generate())
                    .accountType(Account.AccountType.CURRENT)
                    .balance(new BigDecimal("100000.00"))
                    .currency("INR")
                    .status(Account.AccountStatus.ACTIVE)
                    .user(bob)
                    .build();
            accountRepository.save(bobCurrent);

            log.info("==============================================");
            log.info("  FinFlow Demo Data Loaded Successfully");
            log.info("==============================================");
            log.info("  Admin   : admin@finflow.com / Admin@1234");
            log.info("  User 1  : alice@finflow.com / Alice@1234");
            log.info("  User 2  : bob@finflow.com   / Bob@12345");
            log.info("==============================================");
            log.info("  Swagger UI: http://localhost:8080/swagger-ui.html");
            log.info("  H2 Console: http://localhost:8080/h2-console");
            log.info("==============================================");
        };
    }
}
