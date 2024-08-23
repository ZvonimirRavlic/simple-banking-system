package com.project.leapwise.service.impl;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.CustomerRepository;
import com.project.leapwise.dto.CustomerResp;
import com.project.leapwise.mapper.CustomerMapper;
import com.project.leapwise.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    @Override
    public CustomerResp getCustomer(Long customerId) {
        final Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Customer with id: %d doesn't exist!", customerId)));

        final List<Account> accounts = accountRepository.findByCustomerId(customerId);
        final BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return customerMapper.toCustomerResp(customer, accounts, totalBalance);
    }

    @Override
    public Customer findById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Customer with id: %d doesn't exist!", customerId)));
    }
}
