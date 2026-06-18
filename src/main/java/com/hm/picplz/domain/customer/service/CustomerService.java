package com.hm.picplz.domain.customer.service;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.dto.CustomerDto;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.global.error.ExceptionFactory;

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
        // 마지막 닉네임 중복 확인
        memberService.checkNickname(createCustomerRequest.getNickname());

        // 1. social_code로 기존 Member 조회 또는 생성
        Member member = memberService.findOrCreateMember(
            MemberDto.CreateMemberRequest.of(createCustomerRequest, Role.CUSTOMER)
        );

        // 2. 이미 Customer 프로필이 있는지 확인
        if (member.getCustomer() != null) {
            throw ExceptionFactory.of(MemberErrorCode.ALREADY_CUSTOMER);
        }

        // 3. Customer 프로필 생성
        Customer customer = Customer.builder()
                .member(member)
                .build();
        customerRepository.save(customer);

        // 4. Member의 role을 CUSTOMER로 업데이트
        member.updateRole(Role.CUSTOMER);
    }
}
