package com.project.leapwise.integration;

import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.repository.CustomerRepository;
import com.project.leapwise.dto.CustomerResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerIntegrationTests extends AbstractIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void getCustomer__noCustomer() {
        //GIVEN

        //WHEN
        ResponseEntity<CustomerResp> responseEntity = this.restTemplate.getForEntity("http://localhost:" + port + "/customer/-1", CustomerResp.class);

        //THEN
        assertEquals(404, responseEntity.getStatusCode().value());

    }

    @Test
    void getCustomer() {
        //GIVEN
        final Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setPhoneNumber("+385991938924");
        customer.setAddress("address 123");
        customer.setName("Name");
        customer.setEmail("mail@mail.com");
        customerRepository.save(customer);
        //WHEN
        ResponseEntity<CustomerResp> responseEntity = this.restTemplate.getForEntity("http://localhost:" + port + "/customer/1", CustomerResp.class);

        //THEN
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(1L, responseEntity.getBody().customerId());
    }

}
