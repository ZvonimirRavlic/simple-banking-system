package com.project.leapwise.integration;

import com.project.leapwise.LeapwiseApplication;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = LeapwiseApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTests {
}
