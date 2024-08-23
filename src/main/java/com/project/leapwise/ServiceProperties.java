package com.project.leapwise;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Component
@Validated
@ConfigurationProperties(prefix = "service-properties")
@Getter
@Setter
public class ServiceProperties {

    private String accountTurnoverUpdateCron = "0 0 0 1 * ?";
    private String transactionMailTemplate = "transaction-mail";
    private DataGenerationProperties dataGenerationProperties = new DataGenerationProperties();

    @Getter
    @Setter
    public static class DataGenerationProperties {

        private boolean importData = true;

        private String customersFile = "src/main/resources/data/customers.csv";
        private String accountsFile = "src/main/resources/data/accounts.csv";
        private String transactionsFile = "src/main/resources/data/transactions.csv";

        private Long numberOfCustomers = 100L;
        private Long numberOfAccounts = 1000L;
        private Long numberOfTransactions = 100000L;

        private Integer threadPoolSize = 10;
        private Integer batchSize = 1000;
        private String dataSeparator = ",";
    }
}
