# 🧠 Secure Chatbot API Platform — Case Study (Sales-Assistant)

## 1️⃣ Overview

This project demonstrates the **backend architecture** for a secure, role-based **AI chatbot platform** that enables enterprise users to query sales data, generate summaries, and trigger workflow actions.

Built using **Spring Boot (Java 21)** and integrated with **OpenAI / Ollama** for LLM interactions, it enforces **RBAC**, **ABAC**, and **audit logging**, achieving the target success metrics:

| Metric                            | Target   | Achieved   |
| --------------------------------- | -------- | ---------- |
| Access violations prevented       | ≥ 99 %   | **99 %**   |
| P95 latency (excluding async LLM) | < 500 ms | **460 ms** |
| Audit logging coverage            | 100 %    | **100 %**  |

---

## 2️⃣ Component Architecture

```mermaid
flowchart TB
  %% Clients
  subgraph Clients[Client Applications]
    Web[Web / Mobile / Postman / CLI]
  end

  %% API Gateway / Security Edge
  Clients --> APIGW[API Gateway Layer
• JWT Authentication / Guest Access
• RBAC (ADMIN, ANALYST, GUEST)
• Global Audit Logging]

  %% Service
  APIGW --> SA[Sales-Assistant Service (Spring Boot)]

  %% Internal Modules of Service
  subgraph Modules[Sales-Assistant Modules]
    Auth[Auth & RBAC (Spring Security + JWT)]
    Conv[Conversations & Messages (JPA + PostgreSQL)]
    KB[Knowledge Base (Chunked KB + ABAC Policy)]
    Strat[LLM Provider Strategy (OpenAI / Ollama)]
    Audit[Audit Logging & Metrics]
  end

  SA --> Modules

  %% Persistence
  DB[(PostgreSQL)]
  Conv --- DB
  Audit --- DB

  %% Future async ops (designed, optional)
  Ops[(Operations Table for Async Jobs)]
  DB -. future .- Ops

  %% External LLM Providers
  Strat --> OpenAI[OpenAI: gpt-4o-mini]
  Strat --> Ollama[Ollama: llama3.2:latest]
```

---

## 3️⃣ Core Use Cases

| Role        | Description                                              | Endpoints                                                                                      |
| ----------- | -------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **Guest**   | Interact anonymously without history.                    | `POST /api/v1/messages:complete`                                                               |
| **Analyst** | Authenticated user; view/create conversations; query KB. | `POST /v1/conversations`, `POST /v1/conversations/{id}/messages`, `GET /v1/conversations/{id}` |
| **Admin**   | Manage users and inspect audit trails.                   | `GET /v1/audit`, `POST /v1/config/providers` *(future)*                                        |

---

## 4️⃣ Sequence Diagram (Chat Flow)

```
User → ChatController : POST /v1/conversations
ChatController → ConversationService : createAndComplete()
ConversationService → GroundedChatService : process()
GroundedChatService → KbRetriever : topK(userQuery, k)
KbRetriever → KbPolicy : canSee(userContext, chunk)
KbPolicy → GroundedChatService : allowedChunks
GroundedChatService → LlmProviderRouter : get(activeProvider)
LlmProviderRouter → OpenAI / Ollama Adapter : chat()
Adapter → LLM Model : prompt(systemPrompt + context)
Adapter → GroundedChatService : answer
GroundedChatService → ConversationService : {answer, citations}
ConversationService → DB : saveConversation(), saveMessages()
ConversationService → ChatController : 200 OK + LLM Response
ChatController → User : JSON {conversationId, answer, citations}
```

---

## 5️⃣ Design Highlights

### 🔐 **RBAC + ABAC**

* **RBAC** via Spring Security and JWT:

    * `ROLE_ADMIN`, `ROLE_ANALYST`, `ROLE_GUEST`
* **ABAC** for fine-grained KB access:

    * Chunks tagged by region/product (e.g. `apac`, `iphone`)
    * `KbPolicy` filters context based on user roles/tags

### ⚙️ **LLM Provider Strategy**

* **Strategy Pattern** selects provider:

    * `openai` → `OpenAiLlmProvider`
    * `ollama` → `OllamaLlmProvider`
* Configurable in `application.yml`
* Both adapters tested with `MockRestServiceServer`

### 🧩 **Knowledge Base**

* Simple text KB (`kb.txt`) chunked by section headers.
* Indexed in memory with metadata (tags, title).
* Supports top-K retrieval and citation return.

### 🧮 **Persistence Schema**

```sql
CREATE TABLE conversations (
  id UUID PRIMARY KEY,
  title TEXT,
  created_at TIMESTAMP,
  metadata JSONB
);

CREATE TABLE messages (
  id UUID PRIMARY KEY,
  conversation_id UUID REFERENCES conversations(id),
  role TEXT,
  content TEXT,
  citations JSONB,
  created_at TIMESTAMP,
  idempotency_key TEXT
);
```

Indexes:

```sql
CREATE INDEX idx_msg_convo_id ON messages(conversation_id);
CREATE INDEX idx_msg_created_at ON messages(created_at);
CREATE INDEX idx_msg_role ON messages(role);
```

---

## 6️⃣ Audit Logging & Security

* **`AuditLoggingFilter`** logs every request (`user, role, path, status, duration, ip`).
* **`LoggingAccessDeniedHandler` / LoggingAuthEntryPoint`** capture 401 / 403 events.
* Logs written to both **console** and **`logs/audit.log`**:

```
2025-10-26 20:55:23 [AUDIT] user=guest roles=[ROLE_GUEST] method=POST path="/api/v1/messages:complete" status=200 durationMs=189
2025-10-26 20:55:25 [AUTH] 403 Forbidden path=/api/v1/conversations
```

This single log stream serves as evidence for **RBAC and audit-coverage metrics**.

---

## 7️⃣ Performance Metric (P95 < 500 ms)

* Measured via `curl -w` and Postman.
* Excluded network/LLM latency.
* 50 runs → P95 ≈ **460 ms**.

| Percentile | Time (ms) | Note            |
| ---------- | --------- | --------------- |
| P50        | 210       | Median          |
| P90        | 380       | –               |
| **P95**    | **460**   | ✅ Within target |
| P99        | 540       | Rare outlier    |

Planned enhancement: **async task queue ( Redis + Spring @Async )** returning `202 Accepted` for long LLM jobs.

---

## 8️⃣ Success Metrics Summary

| Metric                        | Target   | Achieved   | Validation Method                              |
| ----------------------------- | -------- | ---------- | ---------------------------------------------- |
| **RBAC violations prevented** | ≥ 99 %   | **99 %**   | Counted 401/403 events from `audit.log`        |
| **P95 API latency**           | < 500 ms | **460 ms** | Measured via Postman (excl. LLM)               |
| **Audit logging coverage**    | 100 %    | **100 %**  | Verified 1 log per request in `logs/audit.log` |

---

## 9️⃣ Test Coverage Summary

| Layer            | Tool / Framework      | Example Test                                     |
| ---------------- | --------------------- | ------------------------------------------------ |
| Controller       | MockMvc + JUnit 5     | `GuestControllerTest`                            |
| Service          | Mockito               | `ConversationServiceImplTest`                    |
| LLM Adapters     | MockRestServiceServer | `OpenAiLlmProviderTest`, `OllamaLlmProviderTest` |
| Security         | Spring Security Test  | `AuthContextsTest`                               |
| Audit            | Logback ListAppender  | `AuditLoggingFilterTest`                         |
| Persistence      | H2 @ DataJpaTest      | `MessageRepositoryTest`                          |
| Retrieval/Policy | Pure JUnit            | `GroundedChatServiceTest`, `KbPolicyTest`        |

---

## 🔜 10️⃣ Future Enhancements

* **Async LLM Pipeline** — Redis Queue + Worker Service for long running tasks
* **Workflow Actions** — Report generation triggers
* **Admin Console** — Manage users & view audit dashboard
* **Prometheus/Grafana** — Real-time metrics and latency monitoring

---

## 📦 11️⃣ How to Run

```bash
# Build & run locally
./gradlew clean build
docker-compose up  # (spins up PostgreSQL + optional Ollama)
java -jar build/libs/sales-assistant.jar
```

Access endpoints:

* Guest chat: `POST /api/v1/messages:complete`
* Analyst: `POST /v1/conversations`
* Logs: `logs/audit.log`

---

## 12️⃣ Appendix – Artifacts for Submission

| Artifact           | Purpose                                         |
| ------------------ | ----------------------------------------------- |
| `README.md`        | Architecture & metrics summary                  |
| `logs/audit.log`   | Evidence for RBAC & audit coverage              |
| `perf_metrics.txt` | 50-request latency sample                       |
| `kb.txt`           | Demo knowledge base (Apple Sales FY2023–FY2025) |
| `tests/`           | JUnit suite (> 90 % coverage)                   |
