package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateCustomerRequest;
import com.faris.enterprise_order_api.dto.CustomerResponse;
import com.faris.enterprise_order_api.dto.UpdateCustomerRequest;
import com.faris.enterprise_order_api.exception.CustomerNotFoundException;
import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request
    ) {
        Customer customer = new Customer(
                request.name(),
                request.email()
        );

        return toResponse(
                customerRepository.save(customer)
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse updateCustomer(
            Long id,
            UpdateCustomerRequest request
    ) {
        Customer customer = findCustomer(id);

        customer.update(
                request.name(),
                request.email()
        );

        return toResponse(
                customerRepository.save(customer)
        );
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);

        customerRepository.delete(customer);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }
}