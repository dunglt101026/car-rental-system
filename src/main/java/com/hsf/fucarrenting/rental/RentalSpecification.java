package com.hsf.fucarrenting.rental;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalSpecification {

    public static Specification<CarRental> filterRentals(String keyword, String status) {
        return (root, query, cb) -> {
            if (CarRental.class.equals(query.getResultType())) {
                root.fetch("customer", JoinType.LEFT).fetch("account", JoinType.LEFT);
                root.fetch("car", JoinType.LEFT).fetch("producer", JoinType.LEFT);
                root.fetch("review", JoinType.LEFT);
                query.orderBy(cb.desc(root.get("carRentId")));
            }

            List<Predicate> predicates = new ArrayList<>();

            RentalStatus.from(status).ifPresent(s -> predicates.add(cb.equal(root.get("status"), s)));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate customerName = cb.like(cb.lower(root.get("customer").get("fullName")), pattern);
                Predicate customerMobile = cb.like(cb.lower(root.get("customer").get("mobile")), pattern);
                Predicate carName = cb.like(cb.lower(root.get("car").get("carName")), pattern);
                predicates.add(cb.or(customerName, customerMobile, carName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CarRental> filterReport(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (CarRental.class.equals(query.getResultType())) {
                root.fetch("customer", JoinType.LEFT).fetch("account", JoinType.LEFT);
                root.fetch("car", JoinType.LEFT).fetch("producer", JoinType.LEFT);
                root.fetch("review", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pickupDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pickupDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
