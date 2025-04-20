package com.example.bankcards.repository;

import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {
    List<Card> findAllByUserIdAndIsDeletedFalse(UUID userId);

    List<Card> findAllByStatus(CardStatus status);

    List<Card> findAllByUserId(UUID userId);

    boolean existsByCardNumberEncrypted(String cardNumberEncrypted);
}
