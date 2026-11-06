# Library Management REST API (Core Java)

A small library system written in **plain Core Java 17+ with no external libraries**. It lends books and magazines, enforces borrowing rules, calculates late fines, and exposes everything as a REST API using the JDK's built-in HTTP server.

## What it demonstrates
- **OOP:** abstract class `Item` with `Book` and `Magazine` subclasses (inheritance, abstraction, polymorphism: each type has its own loan period and fine rate); `ItemRepository` interface with an in-memory implementation (programming to an interface).
- **Collections and Streams:** `LinkedHashMap` storage, `stream().filter()` search, borrow-limit counting.
- **Exceptions:** custom `ItemNotFoundException` and `LibraryRuleException`, mapped to HTTP 404 / 409; bad input mapped to 400.
- **Layered design:** model, repository, service, API (REST) and util packages.
- **Testing:** 16 dependency-free tests, with an injected `Clock` so late fines can be tested deterministically.

## Business rules
| Item | Loan period | Fine per late day |
|------|-------------|-------------------|
| Book | 14 days | 2.0 |
| Magazine | 7 days | 1.0 |

A member can hold at most 3 items. A borrowed item cannot be borrowed again or deleted.

## Build and run
```bash
javac -d out $(find src -name "*.java")      # Linux / macOS
java -cp out com.piyush.library.Main         # starts on port 8080 (pass another port as an argument)
```
Windows (PowerShell):
```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out com.piyush.library.Main
```

## Run the tests
```bash
java -cp out com.piyush.library.LibraryServiceTest
```

## API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/items` | List all items |
| GET | `/items?q=java` | Search by title or author/issue |
| GET | `/items/{id}` | Get one item |
| POST | `/items` | Add item, body: `{"type":"BOOK","title":"Clean Code","creator":"Robert C. Martin"}` |
| POST | `/items/{id}/borrow?member=piyush` | Borrow an item |
| POST | `/items/{id}/return` | Return an item, response includes any fine |
| DELETE | `/items/{id}` | Delete an item that is not borrowed |

Example:
```bash
curl http://localhost:8080/items
curl -X POST "http://localhost:8080/items/1/borrow?member=piyush"
curl -X POST "http://localhost:8080/items/1/return"
```

## Possible next steps
Persist data with JDBC and PostgreSQL, migrate the REST layer to Spring Boot, add JUnit tests.
