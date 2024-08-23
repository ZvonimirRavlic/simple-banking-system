package com.project.leapwise.controller.impl;

import com.project.leapwise.controller.CustomerController;
import com.project.leapwise.dto.CustomerResp;
import com.project.leapwise.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerControllerImpl implements CustomerController {

    private final CustomerService customerService;

    @Override
    public CustomerResp getCustomer(Long customerId) {
        return customerService.getCustomer(customerId);
    }
}
