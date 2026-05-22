# IssueFlow — Running the Application

## Prerequisites

- **Docker** (with the Compose plugin, v2+)
- **Java 21+** (verify with `java -version`)

---

## 1. Start the Database

Spin up the local PostgreSQL instance using the provided `compose.yml`:

```bash
docker compose up -d
```

This starts a PostgreSQL 16 container with the following defaults:

| Setting  | Value      |
|----------|------------|
| Host     | `localhost` |
| Port     | `5432`     |
| Database | `issueflow` |
| User     | `issueflow` |
| Password | `issueflow` |

To stop the database when you're done:

```bash
docker compose down
```

---

## 2. Install Dependencies & Build

Build the project and package it into a JAR using the Maven wrapper:

```bash
./mvnw clean package -DskipTests
```

> **Permission denied on macOS/Linux?** Run `chmod +x mvnw` first, then retry the command above.

---

## 3. Run the Tests

Execute the full test suite (unit tests + Spring Boot E2E integration tests):

```bash
./mvnw test
```

> This runs both the unit tests and the full end-to-end integration tests via `IssueFlowE2ETest`, which boots the complete Spring context against the live database. Make sure the database container from Step 1 is running before executing tests.

---

## 4. Run the Application

**Option A — Maven wrapper (recommended for development):**

```bash
./mvnw spring-boot:run
```

**Option B — Run the packaged JAR directly:**

```bash
java -jar target/issueflow-0.0.1-SNAPSHOT.jar
```

The server starts on **`http://localhost:8080`** by default.

---

## Quick Start (all-in-one)

```bash
docker compose up -d
chmod +x mvnw          # macOS/Linux only, if needed
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```
