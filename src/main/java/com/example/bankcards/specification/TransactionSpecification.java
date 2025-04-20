package com.example.bankcards.specification;

import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.entities.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(TransactionFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filter.getEndDate()));
            }

            if (filter.getTransactionType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.getTransactionType()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }

            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("card").get("user").get("id"), filter.getUserId()));
            }

            if (filter.getCardId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("card").get("id"), filter.getCardId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

