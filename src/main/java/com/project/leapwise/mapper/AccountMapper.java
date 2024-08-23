package com.project.leapwise.mapper;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.dto.AccountResp;
import com.project.leapwise.dto.AccountTransactionResp;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface AccountMapper {

    AccountResp toAccountResp(Account account);

    AccountTransactionResp toAccountTransactionResp(Account account);

}
