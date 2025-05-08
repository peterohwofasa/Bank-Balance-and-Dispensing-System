SELECT c.id AS client_id, c.surname, a.account_number, a.description, a.balance
FROM client c
         JOIN account a ON c.id = a.client_id
WHERE a.account_type = 'TRANSACTIONAL'
  AND a.balance = (
    SELECT MAX(balance)
    FROM account a2
    WHERE a2.client_id = c.id AND a2.account_type = 'TRANSACTIONAL'
);
