package com.hsf.fucarrenting.auth.repository;

import com.hsf.fucarrenting.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccountName(String accountName);
    Optional<Account> findByEmail(String email);
    boolean existsByAccountName(String accountName);
    boolean existsByEmail(String email);
}

