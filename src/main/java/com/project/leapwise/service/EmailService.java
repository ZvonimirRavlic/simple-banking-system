package com.project.leapwise.service;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;

public interface EmailService {

    void sendTransactionMail(Transaction transaction, Account account);

}
