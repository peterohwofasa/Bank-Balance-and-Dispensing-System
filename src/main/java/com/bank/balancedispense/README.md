# 💳 Bank Balance and Dispensing System (Version 2)

This Spring Boot backend simulates ATM functionality for Discovery Bank, offering clients:

- ✅ **Transactional and currency balance display** with ZAR conversions
- ✅ **ATM cash withdrawals** with optimal denomination calculation
- ✅ **Fallback suggestions** if exact note match is unavailable
- ✅ **SQL-based financial reports** for account position analysis
- ✅ **Structured error handling** with standardized response blocks

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot 3.2+
- Spring Data JPA + H2 In-Memory DB
- SpringDoc OpenAPI (Swagger UI)
- JUnit, Mockito, Testcontainers
- Maven, Lombok

---

## 🚀 Setup & Run

### ✅ Prerequisites

- Java 17+
- Maven 3.8+

### ▶️ Launch Application

```bash
mvn spring-boot:run
```

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

> Default DB: `jdbc:h2:mem:bankdb`  
> Username: `sa` (no password)

---

## 🔍 API Reference

### Base URL

```
http://localhost:8080/discovery-atm
```

### 📘 Balance APIs

| Method | Endpoint                                 | Description                                             |
|--------|------------------------------------------|---------------------------------------------------------|
| GET    | `/queryTransactionalBalances?clientId=1` | View transactional account balances (descending ZAR)    |
| GET    | `/queryCcyBalances?clientId=1`           | View currency account balances (ascending ZAR)          |

### 💸 Withdrawal API

| Method | Endpoint    | Description                                     |
|--------|-------------|-------------------------------------------------|
| POST   | `/withdraw` | Withdraw funds and receive note denominations  |

#### 🔽 Sample Withdraw Request

```json
{
  "clientId": 1,
  "accountNumber": "TX12345",
  "amount": 500.0,
  "atmId": 1
}
```

---

## 🧾 Structured API Responses

All responses include a top-level `result` block:

```json
"result": {
  "success": true,
  "statusCode": 200,
  "statusReason": "Withdrawal completed successfully",
  "fallbackAmount": null
}
```

---

## 📊 SQL Reporting

### 1. Transactional Account with Highest Balance

```sql
SELECT
    C.ID AS CLIENT_ID,
    C.SURNAME,
    A.CLIENT_ACCOUNT_NUMBER,
    A.DISPLAY_BALANCE
FROM CLIENT C
JOIN CLIENT_ACCOUNT A ON C.ID = A.CLIENT_ID
JOIN ACCOUNT_TYPE T ON A.ACCOUNT_TYPE_CODE = T.ACCOUNT_TYPE_CODE
WHERE T.TRANSACTIONAL = TRUE
  AND A.DISPLAY_BALANCE = (
    SELECT MAX(A2.DISPLAY_BALANCE)
    FROM CLIENT_ACCOUNT A2
    JOIN ACCOUNT_TYPE T2 ON A2.ACCOUNT_TYPE_CODE = T2.ACCOUNT_TYPE_CODE
    WHERE A2.CLIENT_ID = C.ID AND T2.TRANSACTIONAL = TRUE
);
```

### 2. Aggregate Financial Position

```sql
SELECT
    CONCAT(C.TITLE, ' ', C.NAME, ' ', C.SURNAME) AS CLIENT,
    SUM(CASE WHEN A.ACCOUNT_TYPE_CODE IN ('PLOAN', 'HLOAN') THEN A.DISPLAY_BALANCE ELSE 0 END) AS LOAN_BALANCE,
    SUM(CASE WHEN T.TRANSACTIONAL = TRUE THEN A.DISPLAY_BALANCE ELSE 0 END) AS TRANSACTIONAL_BALANCE,
    SUM(CASE WHEN A.ACCOUNT_TYPE_CODE IN ('PLOAN', 'HLOAN') OR T.TRANSACTIONAL = TRUE THEN A.DISPLAY_BALANCE ELSE 0 END) AS NET_POSITION
FROM CLIENT C
JOIN CLIENT_ACCOUNT A ON C.ID = A.CLIENT_ID
JOIN ACCOUNT_TYPE T ON A.ACCOUNT_TYPE_CODE = T.ACCOUNT_TYPE_CODE
GROUP BY C.ID, C.TITLE, C.NAME, C.SURNAME;
```

---

## ❗ Global Exception Handling

Handled with `@RestControllerAdvice`, all exceptions return consistent JSON responses.

**Custom exceptions include:**

- `NoAccountsFoundException`
- `InsufficientFundsException`
- `NoteCalculationException`
- `ATMNotFoundException`
- `AccountNotFoundException`

Each yields a `result` block with appropriate `statusReason` and optional `fallbackAmount`.

---

## 🧪 Testing Overview

### ✅ Unit Tests

- `BalanceServiceImplTest`
- `WithdrawServiceImplTest`
- `BalanceControllerTest`
- `WithdrawControllerTest`
- `NoteCalculatorTest`

### ✅ Integration Tests

- `BalanceControllerIntegrationTest`
- `WithdrawControllerIntegrationTest`

```bash
# Run full test suite
mvn test
```

---

## 📂 Repository

> [GitHub – Bank Balance and Dispensing System](https://github.com/peterohwofasa/Bank-Balance-and-Dispensing-System.git)

---

## 📌 Notes

- H2 schema uses `IDENTITY` strategy per version 2.2.x+ compatibility.
- All currency conversions and overdraft logic handled dynamically.
- Result wrapping and error messaging support future extensibility.
- Swagger/OpenAPI 3 used for API documentation.

---

## 👤 Author

**Peter Ohwofasa**  
Pretoria, South Africa  
[LinkedIn Profile](https://www.linkedin.com/in/peter-ohwofasa/)

---

## 📄 License

This codebase is intended for Discovery Bank technical assessment only.