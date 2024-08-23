package com.project.leapwise.data_import.impl;

import com.opencsv.exceptions.CsvValidationException;
import com.project.leapwise.ServiceProperties;
import com.project.leapwise.data_import.DataImport;
import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.CustomerRepository;
import com.project.leapwise.db.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DataImportRunner implements CommandLineRunner {

    private final DataImport dataImport;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ServiceProperties serviceProperties;


    @Override
    public void run(String... args) {
        if (serviceProperties.getDataGenerationProperties().isImportData()) {
            try {
                dataImport.importData(serviceProperties.getDataGenerationProperties().getCustomersFile(), customerRepository, Customer.class);
                dataImport.importData(serviceProperties.getDataGenerationProperties().getAccountsFile(), accountRepository, Account.class);
                dataImport.importData(serviceProperties.getDataGenerationProperties().getTransactionsFile(), transactionRepository, Transaction.class);

            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
        }
    }
}
