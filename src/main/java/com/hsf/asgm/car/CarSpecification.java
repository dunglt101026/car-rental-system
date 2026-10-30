package com.hsf.asgm.car;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarSpecification {

    public static Specification<Car> filterCars(
            String status,
            Integer producerId,
            Integer capacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {
        return (root, query, cb) -> {
            // Eager fetch producer for entity queries (avoid in count queries)
            if (Car.class.equals(query.getResultType())) {
                root.fetch("producer", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            CarStatus.from(status).ifPresent(s -> predicates.add(cb.equal(root.get("status"), s)));

            if (producerId != null) {
                predicates.add(cb.equal(root.get("producer").get("producerId"), producerId));
            }

            if (capacity != null) {
                predicates.add(cb.equal(root.get("capacity"), capacity));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rentPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rentPrice"), maxPrice));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("carName")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            // Default order by carId DESC (newest cars first) - only on entity result query
            if (Car.class.equals(query.getResultType())) {
                query.orderBy(cb.desc(root.get("carId")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

