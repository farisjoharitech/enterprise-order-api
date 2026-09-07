package com.faris.enterprise_order_api.specification;

import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.model.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSpecificationTest {

    @Test
    void hasStatusCreatesEqualPredicate() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<String> statusPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(statusPath, "CREATED"))
                .thenReturn(predicate);

        Specification<Order> specification =
                OrderSpecification.hasStatus("CREATED");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNotNull(result);

        verify(root).<String>get("status");

        verify(criteriaBuilder)
                .equal(statusPath, "CREATED");
    }

    @Test
    void hasStatusReturnsNullWhenStatusIsNull() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.hasStatus(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void hasStatusReturnsNullWhenStatusIsBlank() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.hasStatus("   ");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void hasCustomerIdCreatesEqualPredicate() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<Customer> customerPath = mock(Path.class);
        Path<Long> customerIdPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Customer>get("customer"))
                .thenReturn(customerPath);

        when(customerPath.<Long>get("id"))
                .thenReturn(customerIdPath);

        when(criteriaBuilder.equal(customerIdPath, 10L))
                .thenReturn(predicate);

        Specification<Order> specification =
                OrderSpecification.hasCustomerId(10L);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNotNull(result);

        verify(root).<Customer>get("customer");

        verify(customerPath).<Long>get("id");

        verify(criteriaBuilder)
                .equal(customerIdPath, 10L);
    }

    @Test
    void hasCustomerIdReturnsNullWhenCustomerIdIsNull() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.hasCustomerId(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void productNameContainsCreatesLikePredicate() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<String> productPath = mock(Path.class);
        Expression<String> lowerExpression = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get("productName"))
                .thenReturn(productPath);

        when(criteriaBuilder.lower(productPath))
                .thenReturn(lowerExpression);

        when(criteriaBuilder.like(
                lowerExpression,
                "%laptop%"
        )).thenReturn(predicate);

        Specification<Order> specification =
                OrderSpecification.productNameContains("Laptop");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNotNull(result);

        verify(root).<String>get("productName");

        verify(criteriaBuilder)
                .lower(productPath);

        verify(criteriaBuilder)
                .like(
                        lowerExpression,
                        "%laptop%"
                );
    }

    @Test
    void productNameContainsReturnsNullWhenProductNameIsNull() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.productNameContains(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void productNameContainsReturnsNullWhenProductNameIsBlank() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.productNameContains("   ");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void minimumQuantityCreatesGreaterThanOrEqualPredicate() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<Integer> quantityPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Integer>get("quantity"))
                .thenReturn(quantityPath);

        when(criteriaBuilder.greaterThanOrEqualTo(
                quantityPath,
                5
        )).thenReturn(predicate);

        Specification<Order> specification =
                OrderSpecification.quantityGreaterThanOrEqualTo(5);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNotNull(result);

        verify(root).<Integer>get("quantity");

        verify(criteriaBuilder)
                .greaterThanOrEqualTo(
                        quantityPath,
                        5
                );
    }

    @Test
    void minimumQuantityReturnsNullWhenMinimumIsNull() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.quantityGreaterThanOrEqualTo(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }

    @Test
    void maximumQuantityCreatesLessThanOrEqualPredicate() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<Integer> quantityPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Integer>get("quantity"))
                .thenReturn(quantityPath);

        when(criteriaBuilder.lessThanOrEqualTo(
                quantityPath,
                10
        )).thenReturn(predicate);

        Specification<Order> specification =
                OrderSpecification.quantityLessThanOrEqualTo(10);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNotNull(result);

        verify(root).<Integer>get("quantity");

        verify(criteriaBuilder)
                .lessThanOrEqualTo(
                        quantityPath,
                        10
                );
    }

    @Test
    void maximumQuantityReturnsNullWhenMaximumIsNull() {
        Root<Order> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Order> specification =
                OrderSpecification.quantityLessThanOrEqualTo(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);
    }
}