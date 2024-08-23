package com.project.leapwise.mapper;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.dto.CustomerResp;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = AccountMapper.class
)
public interface CustomerMapper {
    CustomerResp toCustomerResp(Customer customer, List<Account> accounts, BigDecimal totalBalance);

}
