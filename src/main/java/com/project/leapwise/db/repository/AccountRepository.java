package com.project.leapwise.db.repository;

import com.project.leapwise.db.entity.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerId(Long customerId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
                            
            DROP TABLE IF EXISTS temp_turnover;
                                          
            CREATE TEMPORARY TABLE temp_turnover AS
            SELECT
            account.account_id AS account_id,
            COALESCE(income.total_income,0) -  COALESCE(expenses.total_expenses, 0) AS turnover
            FROM account left join (
                    SELECT t.RECIPIENT_ACCOUNT_ID AS account_id,
                     COALESCE(SUM(t.amount),0) AS total_income
            FROM Transaction t
            WHERE t.TRANSACTION_STATUS = 'COMPLETED'
            AND EXTRACT(MONTH FROM t.timestamp) = EXTRACT(MONTH FROM DATEADD(MONTH, -1, CURRENT_DATE))
            AND EXTRACT(YEAR FROM t.timestamp) = EXTRACT(YEAR FROM CURRENT_DATE)
            GROUP BY t.RECIPIENT_ACCOUNT_ID
                     ) AS income on account.account_id = income.account_id
            LEFT JOIN (
                    SELECT t.SENDER_ACCOUNT_ID AS account_id,
                   COALESCE(SUM(t.amount), 0)  AS total_expenses
            FROM Transaction t
            WHERE t.TRANSACTION_STATUS = 'COMPLETED'
            AND EXTRACT(MONTH FROM t.timestamp) = EXTRACT(MONTH FROM DATEADD(MONTH, -1, CURRENT_DATE))
            AND EXTRACT(YEAR FROM t.timestamp) = EXTRACT(YEAR FROM CURRENT_DATE)
            GROUP BY t.SENDER_ACCOUNT_ID
                     ) AS expenses
            ON account.account_id = expenses.account_id;
                                                
            UPDATE ACCOUNT
            SET PAST_MONTH_TURNOVER = (
                SELECT tt.turnover
                FROM temp_turnover tt
                WHERE ACCOUNT.account_id = tt.account_id);
                  
            DROP TABLE temp_turnover;
                 
                """, nativeQuery = true)
    void updatePastMonthTurnover();

}
