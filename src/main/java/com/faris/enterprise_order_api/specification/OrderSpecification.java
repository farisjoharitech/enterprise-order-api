package com.faris.enterprise_order_api.specification;

import com.faris.enterprise_order_api.model.Order;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> hasStatus(String status) {
        return (root, query, criteriaBuilder) ->
                status == null || status.isBlank()
                        ? null
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Order> hasCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) ->
                customerId == null
                        ? null
                        : criteriaBuilder.equal(
                        root.get("customer").get("id"),
                        customerId
                );
    }

    public static Specification<Order> productNameContains(String productName) {
        return (root, query, criteriaBuilder) ->
                productName == null || productName.isBlank()
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("productName")),
                        "%" + productName.toLowerCase() + "%"
                );
    }

    public static Specification<Order> quantityGreaterThanOrEqualTo(
            Integer minimumQuantity
    ) {
        return (root, query, criteriaBuilder) ->
                minimumQuantity == null
                        ? null
                        : criteriaBuilder.greaterThanOrEqualTo(
                        root.get("quantity"),
                        minimumQuantity
                );
    }

    public static Specification<Order> quantityLessThanOrEqualTo(
            Integer maximumQuantity
    ) {
        return (root, query, criteriaBuilder) ->
                maximumQuantity == null
                        ? null
                        : criteriaBuilder.lessThanOrEqualTo(
                        root.get("quantity"),
                        maximumQuantity
                );
    }
}