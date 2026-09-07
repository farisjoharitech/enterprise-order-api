package com.faris.enterprise_order_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "customer_name", nullable = true, length = 100)
    private String customerName;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, length = 30)
    private String status;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Order() {
    }

    public Order(Customer customer, String productName, int quantity, String status) {
        this.customer = customer;
        this.customerName = customer != null ? customer.getName() : null;
        this.productName = productName;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public void update(Customer customer, String productName, int quantity, String status) {
        this.customer = customer;
        this.customerName = customer != null ? customer.getName() : null;
        this.productName = productName;
        this.quantity = quantity;
        this.status = status;
    }
}
