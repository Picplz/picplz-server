package com.hm.picplz.domain.customer.service;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.member.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final MemberRepository memberRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Long createCustomer(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(IllegalArgumentException::new);

        Customer customer = Customer.builder()
                .member(member)
                .build();

        customerRepository.save(customer);

        return customer.getId();
    }
}
