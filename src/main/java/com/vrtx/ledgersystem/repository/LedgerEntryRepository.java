package com.vrtx.ledgersystem.repository;

import com.vrtx.ledgersystem.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
