# 💳 Bank Balance and Dispensing System

This Spring Boot backend simulates ATM functionality for Discovery Bank, allowing clients to:

- ✅ View **transactional** and **currency account balances** (with Rand conversions)
- ✅ Perform **cash withdrawals** from transactional accounts
- ✅ Retrieve **dispensed denominations**
- ✅ Generate SQL-based **financial reports**

---

## 🛠️ Technologies Used

- Java 17
- Spring Boot 3.2+
- Spring Data JPA
- H2 In-Memory Database
- SpringDoc OpenAPI (Swagger UI)
- Lombok
- JUnit, Mockito
- Testcontainers (optional)

---

## 🚀 Getting Started

### ✅ Prerequisites

- Java 17+
- Maven 3.8+
- IDE (IntelliJ / VS Code)

### ▶️ Run Locally

```bash
# Run Spring Boot application
mvn spring-boot:run
```

Access H2 console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## 🔍 API Endpoints

Base URL: `http://localhost:8080/discovery-atm`

### 💼 Balance APIs

| Method | Endpoint                                 | Description                                              |
|--------|------------------------------------------|----------------------------------------------------------|
| GET    | `/queryTransactionalBalances?clientId=1` | View transactional account balances (descending order)   |
| GET    | `/queryCcyBalances?clientId=1`           | View currency balances with converted Rand (ascending)   |

### 💸 Withdraw API

| Method | Endpoint      | Description                         |
|--------|---------------|-------------------------------------|
| POST   | `/withdraw`   | Withdraw cash and get denominations |

#### Example Withdraw Request

```json
{
  "clientId": 1,
  "accountNumber": "TX12345",
  "amount": 500.0,
  "atmId": 1
}
```

---

## 📊 SQL Reports

### 1. Highest Transactional Account Per Client

```sql
SELECT c.id AS client_id, c.surname, a.account_number, a.description, a.balance
FROM client c
JOIN account a ON c.id = a.client_id
WHERE a.account_type = 'TRANSACTIONAL'
  AND a.balance = (
    SELECT MAX(balance)
    FROM account a2
    WHERE a2.client_id = c.id AND a2.account_type = 'TRANSACTIONAL'
);
```

### 2. Aggregate Financial Position

```sql
SELECT CONCAT(c.title, ' ', c.name, ' ', c.surname) AS client,
       SUM(CASE WHEN a.account_type = 'LOAN' THEN a.balance ELSE 0 END) AS loan_balance,
       SUM(CASE WHEN a.account_type = 'TRANSACTIONAL' THEN a.balance ELSE 0 END) AS transactional_balance,
       SUM(CASE WHEN a.account_type IN ('TRANSACTIONAL', 'LOAN') THEN a.balance ELSE 0 END) AS net_position
FROM client c
JOIN account a ON c.id = a.client_id
GROUP BY c.id, c.title, c.name, c.surname;
```

---

## ❗ Exception Handling

Custom exceptions include:

- `AccountNotFoundException`
- `ATMNotFoundException`
- `InsufficientFundsException`
- `NoteCalculationException`
- `NoAccountsFoundException`

Handled globally with `@RestControllerAdvice`.

---

## 📚 Swagger UI

Accessible at:

```
http://localhost:8080/swagger-ui.html
```

Provides:

- Endpoint summaries
- Input validation details
- Sample requests & responses

---

## 🧪 Testing

### ✅ Unit Tests
- `BalanceServiceImplTest`
- `WithdrawServiceImplTest`
- `NoteCalculatorTest`

### ✅ Integration Tests
- `BalanceControllerIntegrationTest`
- `WithdrawControllerIntegrationTest`

Run all tests with:

```bash
mvn test
```

---

## 🔮 Optional Future Enhancements

- JWT-based user authentication
- Docker containerization
- Admin reporting dashboard
- Externalized currency service (REST/DB)

---

## 📂 GitHub Repository

Project source code is hosted at:

👉 [https://github.com/peterohwofasa/Bank-Balance-and-Dispensing-System.git](https://github.com/peterohwofasa/Bank-Balance-and-Dispensing-System.git)

---

## 👤 Author

**Peter Ohwofasa**  
Pretoria, South Africa  
[LinkedIn](https://www.linkedin.com/in/peter-ohwofasa/)

---

## 📄 License

This project is developed for Discovery Bank technical evaluation purposes.
