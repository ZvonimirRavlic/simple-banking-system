package com.project.leapwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeapwiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeapwiseApplication.class, args);
    }

}
