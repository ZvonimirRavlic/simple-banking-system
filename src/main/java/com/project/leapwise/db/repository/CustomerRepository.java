package com.project.leapwise.db.repository;

import com.project.leapwise.db.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
