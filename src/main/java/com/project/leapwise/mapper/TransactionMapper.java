package com.project.leapwise.mapper;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = AccountMapper.class
)
public interface TransactionMapper {
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    Transaction toTransactionEntity(TransactionReq transactionReq);

    TransactionResp toTransactionResp(Transaction transaction);

    TransactionDetailedResp toTransactionDetailedResp(Transaction transaction, Account senderAccount, Account recipientAccount);


}
