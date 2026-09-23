package com.finflow.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AccountNumberGenerator {

    private static final String PREFIX = "FF";
    private static final AtomicLong counter = new AtomicLong(1000);

    public String generate() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        long seq = counter.getAndIncrement();
        return String.format("%s%s%06d", PREFIX, datePart, seq);
    }
}
