-- Reset tables in correct dependency order
DELETE FROM ATMNOTE;
DELETE FROM ACCOUNT;
DELETE FROM ATM;
DELETE FROM CLIENT;

-- Insert test client
INSERT INTO CLIENT (id, title, name, surname)
VALUES (1, 'Mr', 'John', 'Doe');

-- Insert test ATM (active)
INSERT INTO ATM (id, location, active)
VALUES (1, 'Test Location', TRUE);

-- Insert transactional account (ZAR)
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (1, 1, 'TX12345', 'Main Transactional Account', 2000.00, 'TRANSACTIONAL', 'ZAR');

-- Insert currency account (USD)
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (2, 1, 'FXUSD01', 'USD Wallet', 150.00, 'CURRENCY', 'USD');

-- Insert currency account (EUR)
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (3, 1, 'FXEUR01', 'EUR Wallet', 300.00, 'CURRENCY', 'EUR');

-- Insert ATM Notes (denominations)
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (1, 1, 200, 10);
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (2, 1, 100, 20);
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (3, 1, 50, 30);
