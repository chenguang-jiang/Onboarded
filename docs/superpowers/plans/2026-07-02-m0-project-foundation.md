# M0 Project Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the initial Onboarding project foundation so the backend can run a health endpoint and the WeChat Mini Program skeleton has the agreed visual tokens and core page structure.

**Architecture:** Use a modular monolith backend under `backend/onboarding-api` and a native WeChat Mini Program under `miniprogram`. The first milestone intentionally exposes only infrastructure and a basic health contract; product features such as login, daily plans, question answering, and AI chat will be implemented in later milestone plans.

**Tech Stack:** Java 21 target, Spring Boot 3.5.x, Maven, JUnit 5, Spring MockMvc, Flyway, MySQL 8, Redis 7, WeChat native Mini Program, TypeScript, WXSS design tokens.

---

## File Structure

```text
backend/onboarding-api/
├── pom.xml
├── src/main/java/com/onboarding/OnboardingApplication.java
├── src/main/java/com/onboarding/common/api/ApiResponse.java
├── src/main/java/com/onboarding/common/health/HealthController.java
├── src/main/resources/application.yml
├── src/main/resources/application-local.yml
├── src/main/resources/db/migration/V1__init_schema.sql
└── src/test/java/com/onboarding/common/health/HealthControllerTest.java

miniprogram/
├── app.json
├── app.ts
├── app.wxss
├── project.config.json
├── project.private.config.json
├── sitemap.json
├── pages/today/index.json
├── pages/today/index.ts
├── pages/today/index.wxml
├── pages/today/index.wxss
├── pages/ai-chat/index.json
├── pages/ai-chat/index.ts
├── pages/ai-chat/index.wxml
├── pages/ai-chat/index.wxss
├── pages/wrongbook/index.json
├── pages/wrongbook/index.ts
├── pages/wrongbook/index.wxml
├── pages/wrongbook/index.wxss
├── pages/profile/index.json
├── pages/profile/index.ts
├── pages/profile/index.wxml
└── pages/profile/index.wxss

docker-compose.yml
.gitignore
README.md
```

## Task 1: Backend Health Contract

**Files:**
- Create: `backend/onboarding-api/pom.xml`
- Create: `backend/onboarding-api/src/main/java/com/onboarding/OnboardingApplication.java`
- Create: `backend/onboarding-api/src/main/java/com/onboarding/common/api/ApiResponse.java`
- Create: `backend/onboarding-api/src/main/java/com/onboarding/common/health/HealthController.java`
- Create: `backend/onboarding-api/src/main/resources/application.yml`
- Create: `backend/onboarding-api/src/main/resources/application-local.yml`
- Create: `backend/onboarding-api/src/test/java/com/onboarding/common/health/HealthControllerTest.java`

- [ ] **Step 1: Write the failing health endpoint test**

Create `HealthControllerTest` expecting `GET /api/health` to return a standard API envelope.

```java
@WebMvcTest(HealthController.class)
class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("onboarding-api"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd backend/onboarding-api
mvn test -Dtest=HealthControllerTest
```

Expected: FAIL because the Spring Boot app, controller, and response envelope do not exist yet.

- [ ] **Step 3: Implement minimal Spring Boot app and controller**

Create a Spring Boot application class, `ApiResponse<T>`, and `HealthController` returning:

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "onboarding-api"
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd backend/onboarding-api
mvn test -Dtest=HealthControllerTest
```

Expected: PASS with 1 test.

## Task 2: Database And Local Runtime Skeleton

**Files:**
- Create: `backend/onboarding-api/src/main/resources/db/migration/V1__init_schema.sql`
- Create: `docker-compose.yml`
- Modify: `backend/onboarding-api/pom.xml`
- Modify: `backend/onboarding-api/src/main/resources/application.yml`
- Modify: `backend/onboarding-api/src/main/resources/application-local.yml`

- [ ] **Step 1: Add Flyway and database dependencies**

Add MySQL, Redis, Flyway, validation, and actuator dependencies to the Maven project.

- [ ] **Step 2: Add initial schema migration**

Create the first minimal database table:

```sql
CREATE TABLE app_schema_version_marker (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  marker_key VARCHAR(64) NOT NULL UNIQUE,
  marker_value VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 3: Add Docker Compose**

Define `onboarding-mysql` and `onboarding-redis` services with local ports `3306` and `6379`.

- [ ] **Step 4: Run backend tests**

Run:

```bash
cd backend/onboarding-api
mvn test
```

Expected: PASS. The health test should not require a running database because the test uses `@WebMvcTest`.

## Task 3: Mini Program Skeleton And Visual Tokens

**Files:**
- Create all files under `miniprogram/` listed in the file structure.

- [ ] **Step 1: Create Mini Program config**

Create `app.json`, `project.config.json`, `sitemap.json`, and `app.ts`.

- [ ] **Step 2: Add global WXSS design tokens**

Create `app.wxss` with tokens from `docs/05-ui-design-prompt-from-video.md`: cool white background, soft blue/purple/pink gradients, primary blue, success green, wrong-question rose, glass cards, soft shadows, and safe-area bottom spacing.

- [ ] **Step 3: Create four MVP tab pages**

Create placeholder but styled pages:

```text
pages/today/index
pages/ai-chat/index
pages/wrongbook/index
pages/profile/index
```

Each page must use realistic Onboarding copy and the visual language from the UI prompt.

- [ ] **Step 4: Validate Mini Program file structure**

Run:

```bash
find miniprogram -type f | sort
```

Expected: all config and page files are present.

## Task 4: Project Documentation And Ignore Rules

**Files:**
- Create: `.gitignore`
- Create: `README.md`

- [ ] **Step 1: Add ignore rules**

Ignore Java build output, Node dependencies, local env files, IDE files, logs, and temporary video extraction folders.

- [ ] **Step 2: Add README quick start**

Document:

```text
Backend: cd backend/onboarding-api && mvn spring-boot:run
Backend test: cd backend/onboarding-api && mvn test
Infrastructure: docker compose up -d mysql redis
Mini Program: open miniprogram in WeChat DevTools
```

- [ ] **Step 3: Verify project tree**

Run:

```bash
find . -maxdepth 3 -type f | sort
```

Expected: docs, backend, miniprogram, Docker Compose, README, and gitignore are present.

## Self-Review

Spec coverage:

- Covers M0 project initialization from `docs/04-execution-plan.md`.
- Covers backend health endpoint, local runtime config, database migration, Docker Compose, Mini Program skeleton, and UI tokens.
- Does not cover login, today task generation, question answering, wrongbook behavior, or GLM integration; those belong to later milestone plans.

Placeholder scan:

- No `TODO`, `TBD`, or `fill later` placeholders are used.

Type consistency:

- The health test expects `ApiResponse<Map<String, String>>`.
- The controller returns `status` and `service` fields exactly matching the test.
