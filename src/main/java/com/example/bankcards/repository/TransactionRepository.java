package com.example.bankcards.repository;

import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.Transaction;
import com.example.bankcards.entities.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM Transaction t
                WHERE t.card = :card
                  AND t.timestamp >= :from
                  AND t.type IN (:types)
            """)
    BigDecimal sumAmountByCardAndTimestampAfter(
            @Param("card") Card card,
            @Param("from") LocalDateTime from,
            @Param("types") List<TransactionType> types);

}
