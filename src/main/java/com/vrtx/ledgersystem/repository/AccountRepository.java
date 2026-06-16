package com.vrtx.ledgersystem.repository;

import com.vrtx.ledgersystem.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountType(Account.AccountType accountType);
}
