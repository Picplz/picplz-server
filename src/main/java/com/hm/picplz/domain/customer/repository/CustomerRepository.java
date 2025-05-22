package com.hm.picplz.domain.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.customer.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
