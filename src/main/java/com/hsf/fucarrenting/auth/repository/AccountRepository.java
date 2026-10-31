package com.hsf.fucarrenting.auth.repository;

import com.hsf.fucarrenting.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    /**
     * The schema has no UNIQUE constraint, so a username or email may match several rows.
     */
    List<Account> findByAccountNameOrEmailOrderByAccountIdAsc(String accountName, String email);
    boolean existsByAccountName(String accountName);
    boolean existsByEmail(String email);
}

