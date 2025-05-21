package com.hm.picplz.domain.customer.service;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.dto.CustomerDto;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final MemberService memberService;
    private final CustomerRepository customerRepository;

    @Transactional
    public void createCustomer(CustomerDto.CreateCustomerRequest createCustomerRequest) {
        Member member = memberService.createMember(MemberDto.CreateMemberRequest.of(createCustomerRequest, Role.CUSTOMER));
        Customer customer = Customer.builder()
                .member(member)
                .build();

        customerRepository.save(customer);
    }
}
