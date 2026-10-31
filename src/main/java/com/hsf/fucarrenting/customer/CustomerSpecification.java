package com.hsf.fucarrenting.customer;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static Specification<Customer> filterCustomers(String keyword) {
        return (root, query, cb) -> {
            if (Customer.class.equals(query.getResultType())) {
                root.fetch("account", JoinType.LEFT);
                query.orderBy(cb.desc(root.get("customerId")));
            }

            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("mobile")), pattern));
            predicates.add(cb.like(cb.lower(root.get("identityCard")), pattern));
            predicates.add(cb.like(cb.lower(root.get("licenceNumber")), pattern));

            // Search by associated account username & email
            predicates.add(cb.like(cb.lower(root.get("account").get("accountName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("account").get("email")), pattern));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}

