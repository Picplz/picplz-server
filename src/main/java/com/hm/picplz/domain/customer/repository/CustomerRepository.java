package com.hm.picplz.domain.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hm.picplz.domain.customer.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.member.id = :memberId")
    Optional<Customer> findByMemberId(@Param("memberId") Long memberId);


}
