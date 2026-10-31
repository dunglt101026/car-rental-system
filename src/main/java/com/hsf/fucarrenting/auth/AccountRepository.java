package com.hsf.fucarrenting.auth;

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

