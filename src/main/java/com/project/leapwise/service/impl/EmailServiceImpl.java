package com.project.leapwise.service.impl;

import com.project.leapwise.ServiceProperties;
import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.service.CustomerService;
import com.project.leapwise.service.EmailService;
import com.project.leapwise.type.EmailDetails;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.project.leapwise.type.TransactionStatus.COMPLETED;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;
    private final CustomerService customerService;
    private final ServiceProperties serviceProperties;


    private void sendHtmlTemplateMail(EmailDetails details) {
        try {
            Context context = new Context();
            context.setVariables(details.getVariables());
            String body = templateEngine.process(details.getTemplate(), context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setSubject(details.getSubject());
            mimeMessageHelper.setText(body, true);
            mailSender.send(mimeMessage);
            log.info("Mail has been sent to: {}", details.getRecipient());
        } catch (Exception e) {
            log.error("Mail has not been successfully sent to: {}", details.getRecipient(), e.getCause());
        }
    }

    @Override
    public void sendTransactionMail(Transaction transaction, Account account) {
        try {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient(customerService.findById(account.getCustomerId()).getEmail());
            emailDetails.setTemplate(serviceProperties.getTransactionMailTemplate());
            emailDetails.setSubject("Transaction mail");

            final boolean sender = transaction.getSenderAccountId().equals(account.getAccountId());
            final BigDecimal netAmount = transaction.getAmount()
                    .multiply(sender ? BigDecimal.ONE : BigDecimal.valueOf(-1));

            final Map<String, Object> variables = new HashMap<>();
            variables.put("transactionId", transaction.getTransactionId());
            variables.put("transactionSuccess", transaction.getTransactionStatus().equals(COMPLETED));
            variables.put("amount", transaction.getAmount());
            variables.put("oldBalance", account.getBalance().add(netAmount));
            variables.put("newBalance", account.getBalance());
            variables.put("sender", sender);
            emailDetails.setVariables(variables);
            sendHtmlTemplateMail(emailDetails);
        } catch (ResponseStatusException e) {
            log.error("Mail has not been successfully sent to accountId: {}", account.getAccountId());
        }
    }
}

