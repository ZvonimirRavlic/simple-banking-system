package com.project.leapwise.service;

import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.dto.CustomerResp;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CustomerService {
    CustomerResp getCustomer(@NotNull Long customerId);

    Customer findById(@NotNull Long customerId);
}
