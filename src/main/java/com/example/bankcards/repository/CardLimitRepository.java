package com.example.bankcards.repository;

import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardLimitRepository extends JpaRepository<CardLimit, UUID>, JpaSpecificationExecutor<CardLimit> {
    Optional<CardLimit> findByCardId(UUID cardId);
    List<CardLimit> findAllByCardId(UUID cardId);
    Optional<CardLimit> findByCardIdAndTransactionType(UUID cardId, TransactionType type);
}
