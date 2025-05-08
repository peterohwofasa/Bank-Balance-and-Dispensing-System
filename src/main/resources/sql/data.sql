
-- Insert test client
INSERT INTO CLIENT (id, title, name, surname) VALUES (1, 'Mr', 'John', 'Doe');

-- Insert test ATM (active)
INSERT INTO ATM (id, location, active) VALUES (1, 'Test Location', TRUE);

-- Insert test Transactional account (ZAR)
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (1, 1, 'TX12345', 'Transactional Account', 2000.0, 'TRANSACTIONAL', 'ZAR');

-- Insert test Currency account (USD)
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (2, 1, 'FX12345', 'USD Wallet', 100.0, 'CURRENCY', 'USD');

-- Optional: EUR Currency account
INSERT INTO ACCOUNT (id, client_id, account_number, description, balance, account_type, currency)
VALUES (3, 1, 'FXEUR123', 'EUR Wallet', 300.0, 'CURRENCY', 'EUR');

-- Insert ATM Notes with unique IDs
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (4, 1, 200, 10);
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (5, 1, 100, 10);
INSERT INTO ATMNOTE (id, atm_id, denomination, quantity) VALUES (6, 1, 50, 10);
