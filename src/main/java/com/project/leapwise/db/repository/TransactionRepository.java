package com.project.leapwise.db.repository;

import com.project.leapwise.db.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
            select transaction from Transaction transaction
            join Account senderAccount on transaction.senderAccountId = senderAccount.accountId
            join Account recipientAccount on transaction.recipientAccountId = recipientAccount.accountId
            where senderAccount.customerId= :customerId or recipientAccount.customerId = :customerId""")
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);

    @Query("""
            select transaction from Transaction transaction
            join Account senderAccount on transaction.senderAccountId = senderAccount.accountId
            join Account recipientAccount on transaction.recipientAccountId = recipientAccount.accountId
            where senderAccount.customerId= :customerId or recipientAccount.customerId = :customerId""")
    Page<Transaction> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}
