package com.example.bankcards.specification;

import com.example.bankcards.dto.card.CardFilterDto;
import com.example.bankcards.entities.Card;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CardSpecification {

    public static Specification<Card> withFilters(CardFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getMinBalance() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("balance"), filter.getMinBalance()));
            }
            if (filter.getMaxBalance() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("balance"), filter.getMaxBalance()));
            }

            if (filter.getIsDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), filter.getIsDeleted()));
            }

            if (filter.getRequestedForBlocking() != null){
                predicates.add(criteriaBuilder.equal(root.get("requestedForBlocking"), filter.getRequestedForBlocking()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
