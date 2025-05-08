SELECT CONCAT(c.title, ' ', c.name, ' ', c.surname) AS client,
       SUM(CASE WHEN a.account_type = 'LOAN' THEN a.balance ELSE 0 END) AS loan_balance,
       SUM(CASE WHEN a.account_type = 'TRANSACTIONAL' THEN a.balance ELSE 0 END) AS transactional_balance,
       SUM(CASE WHEN a.account_type IN ('TRANSACTIONAL', 'LOAN') THEN a.balance ELSE 0 END) AS net_position
FROM client c
         JOIN account a ON c.id = a.client_id
GROUP BY c.id, c.title, c.name, c.surname;
