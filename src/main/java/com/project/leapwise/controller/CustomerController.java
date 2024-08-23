package com.project.leapwise.controller;

import com.project.leapwise.dto.CustomerResp;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
public interface CustomerController {

    @GetMapping("/{id}")
    CustomerResp getCustomer(@PathVariable("id") Long customerId);
}
