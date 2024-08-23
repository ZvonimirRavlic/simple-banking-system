package com.project.leapwise.db.entity;

import com.project.leapwise.type.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private Long senderAccountId;

    private Long recipientAccountId;

    private BigDecimal amount;

    private Long currencyId;

    private String message;

    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus = TransactionStatus.PENDING;

}
