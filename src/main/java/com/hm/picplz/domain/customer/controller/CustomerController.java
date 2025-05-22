package com.hm.picplz.domain.customer.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.customer.dto.CustomerDto;
import com.hm.picplz.domain.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/customers")
@Tag(name = "Customer")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "고객 회원가입")
    @PostMapping
    public void createCustomer(@RequestBody CustomerDto.CreateCustomerRequest createCustomerRequest) {
        customerService.createCustomer(createCustomerRequest);
    }
}
