package com.hsf.fucarrenting.customer.repository;

import com.hsf.fucarrenting.customer.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    @EntityGraph(attributePaths = "account")
    Optional<Customer> findByAccount_AccountId(Integer accountId);
    boolean existsByIdentityCard(String identityCard);
    boolean existsByMobile(String mobile);
    boolean existsByIdentityCardAndCustomerIdNot(String identityCard, Integer customerId);
    boolean existsByMobileAndCustomerIdNot(String mobile, Integer customerId);
}
