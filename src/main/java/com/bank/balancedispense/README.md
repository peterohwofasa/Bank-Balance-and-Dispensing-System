# 💳 Bank Balance and Dispensing System

This project is a Java Spring Boot backend system that simulates core ATM functionality. It enables clients to:

* View **transactional** and **currency account balances**
* Make **cash withdrawals** from transactional accounts
* Retrieve **note denominations** dispensed
* Access **SQL reports** for financial insights

---

## 🛠️ Technologies Used

* Java 17
* Spring Boot 3.4.x
* Spring Data JPA
* H2 In-Memory Database
* SpringDoc OpenAPI (Swagger UI)
* Lombok
* JUnit & Mockito
* Testcontainers (optional)

---

## 🚀 Getting Started

### ✅ Prerequisites

* Java 17+
* Maven 3.8+
* IDE (e.g., IntelliJ, VS Code)

### 📦 Run Locally

```bash

# Run the application
mvn spring-boot:run
```

---

## 🔍 API Endpoints

### 💼 Balance

| Method | Endpoint                             | Description                                  |
| ------ | ------------------------------------ | -------------------------------------------- |
| `GET`  | `/balances/transactional?clientId=1` | View transactional account balances          |
| `GET`  | `/balances/currency?clientId=1`      | View currency accounts with Rand conversions |

### 💸 Withdraw

| Method | Endpoint    | Description                         |
| ------ | ----------- | ----------------------------------- |
| `POST` | `/withdraw` | Withdraw from transactional account |

#### Example Request:

```json
{
  "clientId": 1,
  "accountNumber": "TX12345",
  "amount": 200.0,
  "atmId": 1
}
```

---

## 📊 SQL Reports

### 1. Client Net Position

Shows total loan, transactional, and net balances.

```sql
SELECT CONCAT(c.title, ' ', c.name, ' ', c.surname) AS client,
       SUM(CASE WHEN a.account_type = 'LOAN' THEN a.balance ELSE 0 END) AS loan_balance,
       SUM(CASE WHEN a.account_type = 'TRANSACTIONAL' THEN a.balance ELSE 0 END) AS transactional_balance,
       SUM(CASE WHEN a.account_type IN ('TRANSACTIONAL', 'LOAN') THEN a.balance ELSE 0 END) AS net_position
FROM client c
         JOIN account a ON c.id = a.client_id
GROUP BY c.id, c.title, c.name, c.surname;
```

### 2. Highest Transactional Account Per Client

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

---

## 🔒 Exception Handling

* `AccountNotFoundException`
* `ATMNotFoundException`
* `InsufficientFundsException`
* `NoteCalculationException`
* `NoAccountsFoundException`

Handled globally using `@RestControllerAdvice`.

---

## 📚 Swagger UI

Accessible at:

```
http://localhost:8080/swagger-ui.html
```

Includes:

* API summaries
* Response schemas
* Validation annotations

---

## 🧪 Testing

### ✅ Unit Tests

* `BalanceServiceImplTest`
* `WithdrawServiceImplTest`
* `NoteCalculatorTest`

### ✅ Integration Tests

* `BalanceIntegrationTest`

    * `/balances/transactional`
    * `/balances/currency`
    * `/withdraw`

To run tests:

```bash
mvn test
```

---

## 🧩 Future Enhancements (Optional)

* JWT Authentication / Authorization
* Docker Support
* Frontend Web UI
* External configuration service for exchange rates

---

## 👨‍💻 Author

**Peter Ohwofasa**
Pretoria, South Africa
[LinkedIn](https://www.linkedin.com/in/peter-ohwofasa/)

---

## 📄 License

This project is open source and available under the Discovery License.
