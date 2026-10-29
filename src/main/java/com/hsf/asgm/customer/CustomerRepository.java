package com.hsf.asgm.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByAccount_AccountId(Integer accountId);
    Optional<Customer> findByIdentityCard(String identityCard);
    Optional<Customer> findByMobile(String mobile);
    boolean existsByIdentityCard(String identityCard);
    boolean existsByMobile(String mobile);
}
