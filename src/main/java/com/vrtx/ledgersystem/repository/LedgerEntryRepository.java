package com.vrtx.ledgersystem.repository;

import com.vrtx.ledgersystem.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransactionId(UUID transactionId);

    List<LedgerEntry> findByAccountId(UUID accountId);

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END), 0)
             - COALESCE(SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0)
        FROM LedgerEntry e WHERE e.account.id = :accountId
        """)
    BigDecimal calculateBalance(@Param("accountId") UUID accountId);
}
