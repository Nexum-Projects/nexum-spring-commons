package com.nexum.commons.search;

import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SearchSpecificationUtils {
    private SearchSpecificationUtils() {
    }

    public static <T> Specification<T> activeAndTextQuery(
            String activeField,
            boolean activeValue,
            String query,
            Set<String> searchableFields
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get(activeField), activeValue));
            addTextQueryPredicate(query, searchableFields, root, criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private static <T> void addTextQueryPredicate(
            String query,
            Set<String> searchableFields,
            jakarta.persistence.criteria.Root<T> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<jakarta.persistence.criteria.Predicate> predicates
    ) {
        if (query != null && !query.isBlank() && searchableFields != null && !searchableFields.isEmpty()) {
            // El texto del usuario es literal: se escapan los comodines de LIKE (\, %, _).
            String literal = query.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            String pattern = "%" + literal + "%";
            Expression<String> normalizedPattern = criteriaBuilder.function(
                    "unaccent",
                    String.class,
                    criteriaBuilder.literal(pattern)
            );

            List<jakarta.persistence.criteria.Predicate> searchPredicates = searchableFields.stream()
                    .map(field -> criteriaBuilder.like(
                            criteriaBuilder.function(
                                    "unaccent",
                                    String.class,
                                    criteriaBuilder.lower(root.get(field).as(String.class))
                            ),
                            normalizedPattern,
                            '\\'
                    ))
                    .toList();
            predicates.add(criteriaBuilder.or(searchPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
        }
    }
}
