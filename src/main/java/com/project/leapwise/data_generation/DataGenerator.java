package com.project.leapwise.data_generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.entity.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static com.project.leapwise.type.TransactionStatus.COMPLETED;
import static java.math.BigDecimal.ONE;

public class DataGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Config config = new Config("src/main/resources/application.yml");
    private static final Random r = new Random();

    private void generateCustomers(final Long numberOfCustomers) {
        final List<Customer> customers = new ArrayList<>();
        for (long i = 1; i <= numberOfCustomers; i++) {
            final Customer customer = new Customer();
            customer.setName("Customer" + i);
            customer.setAddress("Address " + i);
            customer.setEmail(String.format("mail%d@leapwisemail.com", i));
            customer.setPhoneNumber("PhoneNumber" + i);
            customers.add(customer);
        }
        writeObjectsToCsv(customers, config.getProperty("customers-file"));
    }

    private void generateAccounts(Long numberOfAccounts, Long numberOfCustomers) {
        final List<Account> accounts = new ArrayList<>();
        for (long i = 1; i <= numberOfAccounts; i++) {
            final Account account = new Account();
            account.setAccountNumber(String.valueOf(i));
            account.setAccountType(PERSONAL);
            account.setBalance(new BigDecimal("100"));
            account.setCustomerId(r.nextLong(numberOfCustomers) + 1);
            account.setPastMonthTurnover(ONE);
            accounts.add(account);
        }
        writeObjectsToCsv(accounts, config.getProperty("accounts-file"));
    }

    private void generateTransactions(Long numberOfTransactions, Long numberOfAccounts) {
        final List<Transaction> transactions = new ArrayList<>();
        for (long i = 1; i <= numberOfTransactions; i++) {
            final Transaction transaction = new Transaction();
            transaction.setSenderAccountId(r.nextLong(numberOfAccounts) + 1);
            transaction.setRecipientAccountId(r.nextLong(numberOfAccounts) + 1);
            transaction.setAmount(BigDecimal.valueOf(r.nextDouble() * 100));
            transaction.setCurrencyId(1L);
            transaction.setMessage("Message " + i);
            transaction.setTimestamp(LocalDateTime.now().minusDays(r.nextInt(365)));
            transaction.setTransactionStatus(COMPLETED);
            transactions.add(transaction);
        }
        writeObjectsToCsv(transactions, config.getProperty("transactions-file"));
    }

    public <T> void writeObjectsToCsv(List<T> objects, String filePath) {
        File file = new File(filePath);
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();

        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer).build();
            beanToCsv.write(objects);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
    }

    public void runGenerators() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        final Long numberOfCustomers = Long.valueOf(config.getProperty("number-of-customers"));
        final Long numberOfAccounts = Long.valueOf(config.getProperty("number-of-accounts"));
        final Long numberOfTransactions = Long.valueOf(config.getProperty("number-of-transactions"));

        executorService.submit(() -> generateCustomers(numberOfCustomers));
        executorService.submit(() -> generateAccounts(numberOfAccounts, numberOfCustomers));
        executorService.submit(() -> generateTransactions(numberOfTransactions, numberOfAccounts));

        executorService.shutdown();
    }

    public static void main(String[] args) {
        objectMapper.registerModule(new JavaTimeModule());
        DataGenerator transactionDataGenerator = new DataGenerator();
        transactionDataGenerator.runGenerators();
    }

}
