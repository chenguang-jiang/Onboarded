# Onboarding

Onboarding is a WeChat Mini Program for reviewing the soft exam system architect syllabus. It is designed as a study coach: daily knowledge points, one question per point, wrong-question review, AI knowledge-base Q&A, and progress tracking.

## Project Structure

```text
backend/onboarding-api  Spring Boot backend
miniprogram             WeChat Mini Program
docs                    Product, architecture, UI, and execution documents
docker-compose.yml      Local MySQL and Redis
```

## Backend

Run tests:

```bash
cd backend/onboarding-api
mvn test
```

Run the API:

```bash
cd backend/onboarding-api
mvn spring-boot:run
```

The default `standalone` profile starts without MySQL or Redis. To run with local infrastructure, start Docker services first and use:

```bash
cd backend/onboarding-api
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Health check:

```bash
curl http://localhost:8080/api/health
```

M1 API smoke flow:

```bash
login_response=$(curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"dev-code-e2e"}')

token=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","token")' <<< "$login_response")

curl -sS -X PUT http://localhost:8080/api/users/me/study-settings \
  -H "Authorization: Bearer $token" \
  -H 'Content-Type: application/json' \
  -d '{"examDate":"2026-11-08","dailyTarget":15,"reminderTime":"08:30"}'

curl -sS http://localhost:8080/api/today \
  -H "Authorization: Bearer $token"
```

M2 closed-loop smoke flow (answer -> wrongbook -> redo x2 -> auto-mastered):

```bash
login_response=$(curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"smoke-m2"}')
token=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","token")' <<< "$login_response")

# Fetch a question; answerKey/explanation are NOT shipped before answering.
curl -sS http://localhost:8080/api/questions/1 -H "Authorization: Bearer $token"

# Submit a wrong answer -> auto-archived into the wrongbook (status OPEN).
wrong_response=$(curl -sS -X POST http://localhost:8080/api/questions/1/answer \
  -H "Authorization: Bearer $token" \
  -H 'Content-Type: application/json' \
  -d '{"selectedAnswer":"B"}')
wq_id=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","wrongQuestionId")' <<< "$wrong_response")

# List the wrongbook (non-mastered items only).
curl -sS http://localhost:8080/api/wrongbook -H "Authorization: Bearer $token"

# Redo correct twice -> auto-MASTERED and hidden from the default list.
curl -sS -X POST http://localhost:8080/api/wrongbook/$wq_id/redo \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"selectedAnswer":"A"}'
curl -sS -X POST http://localhost:8080/api/wrongbook/$wq_id/redo \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"selectedAnswer":"A"}'

# Manual mark mastered (alternative archive path; idempotent).
curl -sS -X POST http://localhost:8080/api/wrongbook/$wq_id/mastered -H "Authorization: Bearer $token"
```

M3 mastery smoke flow (answer/redo updates掌握度 + 薄弱章节):

```bash
login_response=$(curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"smoke-m3"}')
token=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","token")' <<< "$login_response")

# Submit a wrong answer -> mastery score clamps to 0, the chapter becomes weak.
curl -sS -X POST http://localhost:8080/api/questions/1/answer \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"selectedAnswer":"B"}'

# Per-chapter mastery + weak flag (sorted weakest-first).
curl -sS http://localhost:8080/api/progress/chapters -H "Authorization: Bearer $token"

# Overall totals: studied / mastered / weak / averageScore.
curl -sS http://localhost:8080/api/progress/overview -H "Authorization: Bearer $token"
```

Daily-plan smoke flow (per-date plan + start/complete):

```bash
login_response=$(curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"smoke-today"}')
token=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","token")' <<< "$login_response")

# Today's plan is generated on first fetch (15 items, weak/wrong-first) and reused after.
today=$(curl -sS http://localhost:8080/api/today -H "Authorization: Bearer $token")
item_id=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","items",0,"itemId")' <<< "$today")

# Mark an item studying, then complete it (idempotent) -> completedCount increments.
curl -sS -X POST http://localhost:8080/api/today/items/$item_id/start -H "Authorization: Bearer $token"
curl -sS -X POST http://localhost:8080/api/today/items/$item_id/complete -H "Authorization: Bearer $token"
```

AI chat smoke flow (sessions + ask, mock GLM by default):

```bash
login_response=$(curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"smoke-ai"}')
token=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","token")' <<< "$login_response")

# Create a session, then ask. The assistant reply (mock by default) carries content + references.
session_response=$(curl -sS -X POST http://localhost:8080/api/ai/sessions \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"title":"架构风格"}')
session_id=$(ruby -rjson -e 'puts JSON.parse(ARGF.read).dig("data","id")' <<< "$session_response")

curl -sS -X POST http://localhost:8080/api/ai/sessions/$session_id/ask \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"question":"质量属性场景怎么写？"}'

# Fetch the persisted thread (user + assistant messages).
curl -sS http://localhost:8080/api/ai/sessions/$session_id/messages -H "Authorization: Bearer $token"
```

### WeChat Login

The local default uses a mock `code2Session` client so the project can run before AppSecret is configured.

```bash
WECHAT_MINI_APP_ID=wx0395857cc0cc197e
WECHAT_MINI_APP_SECRET=your-secret
WECHAT_MINI_MOCK=false
```

Do not commit `WECHAT_MINI_APP_SECRET`. Keep it in your local shell, IDE run configuration, or deployment secret manager.

### GLM / AI

The local default uses a mock GLM client so the AI chat flow runs without an API key. Switch to the real ZhipuAI GLM-5.2 + knowledge-base retrieval with:

```bash
GLM_API_KEY=your-key
GLM_KNOWLEDGE_ID=2072256899017056256
GLM_MOCK=false
```

`GLM_KNOWLEDGE_ID` defaults to `2072256899017056256` in local configuration, so only `GLM_API_KEY` and `GLM_MOCK=false` are required for your current knowledge base. Do not commit `GLM_API_KEY`. Keep it in your local shell, IDE run configuration, or deployment secret manager. Per-user daily request cap is `glm.max-daily-requests-per-user` (default 100).

## Local Infrastructure

Start MySQL and Redis:

```bash
docker compose up -d mysql redis
```

Default local connection:

```text
MySQL: localhost:3306/onboarding
User: onboarding
Password: onboarding
Redis: localhost:6379
```

## Mini Program

Open the `miniprogram` folder in WeChat DevTools.

The Mini Program currently includes:

- First-run onboarding
- Today
- Answer (practice & redo)
- AI chat
- Wrongbook
- Profile

The visual direction follows `docs/05-ui-design-prompt-from-video.md`.

## Current Milestone

M1 auth, onboarding, and seed content:

- Spring Boot health endpoint.
- Mock/real-switchable WeChat `code2Session` login.
- Study settings API that completes first-run onboarding.
- Seed content APIs for 5 chapters, 15 knowledge points, and one question per point.
- Mini Program first-run onboarding page and API-backed Today page.
- Local MySQL and Redis Compose services.
- First Flyway migration.
- Product design closure review in `docs/06-design-closure-review.md`.

M5 durability and carry-over hardening:

- Login tokens are random session tokens. Under `local`, sessions are stored in Redis with a 30-day TTL; under `standalone`, tests and local no-infra runs use an in-memory fallback.
- Unfinished historical daily-plan items now carry over into the next generated daily plan first, with source `CARRY_OVER`, while completed historical items are ignored and duplicate knowledge points are de-duplicated.
- Chapters, knowledge points, practice questions, and options are persisted in MySQL under the `local` profile via Flyway `V3__persist_content_catalog.sql`; `standalone` continues to use the same seed catalog in memory.
- Consolidated schema and seed SQL is available at `docs/sql/onboarding_mysql_schema.sql`.

M2 answer submission and wrongbook auto-archiving:

- `GET /api/questions/{id}` and `POST /api/questions/{id}/answer` for fetching and judging questions; the correct answer is never shipped before submission.
- `GET /api/wrongbook`, `GET /api/wrongbook/chapters`, `GET /api/wrongbook/{id}`, `POST /api/wrongbook/{id}/redo`, `POST /api/wrongbook/{id}/mastered`.
- A wrong answer auto-archives into the wrongbook; two consecutive correct redos auto-mark it mastered (hidden from the default list). Manual "mark mastered" hides it too.
- `answer_record` and `wrong_question` are persisted under the `local` profile through Flyway `V2`; `standalone` keeps the in-memory implementation for fast tests.
- Mini Program answer page (reused by Today "做题验证" and Wrongbook "重做") and a real, API-backed Wrongbook page with chapter filtering.

M3 mastery tracking and weak-chapter summary:

- `user_mastery` per knowledge point updated on every answer/redo: correct +15, first wrong -15, repeat wrong -10, redo correct +20, redo wrong -10 (clamped 0..100).
- `GET /api/progress/overview` and `GET /api/progress/chapters` (per-chapter studied/mastered/weak counts, average score, weak flag; sorted weakest-first).
- Today page shows a "章节掌握" card with overall metrics and the weakest chapters.
- Mastery updates are written together with `answer_record`/`wrong_question` in the answer/redo services; in-memory `user_mastery` repository matches the M1 pattern.

M2-backfill daily plan generation (today learning loop):

- `GET /api/today` generates a per-date plan of 15 knowledge points on first fetch and reuses it after (one plan per user per date). Items are ordered weak/wrong-first using mastery + wrongbook data (`source`: WRONG_RELATED / LOW_MASTERY / NEW / FALLBACK).
- `POST /api/today/items/{itemId}/start` and `POST /api/today/items/{itemId}/complete` drive the PENDING -> STUDYING -> DONE lifecycle; complete is idempotent and increments the plan's `completedCount`.
- Today page is plan-backed: per-card status badges, completed cards dim out, "今日进度" comes from the plan, and answering correctly completes the item.
- Superseded `/api/study/today` (and its answerKey leak) removed; replaced by `/api/today` whose question payload never includes the answerKey.
- `daily_plan` / `daily_plan_item` are persisted under the `local` profile. Historical unfinished items are explicitly carried into the next daily plan with source `CARRY_OVER`.

M4 GLM-5.2 knowledge-base Q&A:

- `POST /api/ai/sessions`, `GET /api/ai/sessions`, `GET /api/ai/sessions/{id}/messages`, `POST /api/ai/sessions/{id}/ask`.
- Mock/real-switchable GLM client (default mock, no API key needed; real client calls ZhipuAI GLM-5.2 with the retrieval tool and knowledge base `2072256899017056256` when `GLM_MOCK=false`). Per-user daily request cap with 429 on overflow.
- Ask persists the user + assistant messages; sessions are user-scoped (foreign-session access returns 404).
- Mini Program AI page is now live: session auto-create, article-style assistant answers, reference cards, quick prompts, composer, and "加入复习".
- AI assistant messages can be saved into `ai_review_item` through `POST /api/ai/messages/{id}/review-items`; the table is created by Flyway `V4`.
- Real GLM tuning, cost dashboard, and session switching UI remain deferred.

M5-M7 experience hardening:

- Subscription authorization can be saved through `PUT /api/notifications/subscription`; accepted and rejected decisions are persisted in `subscription_preference`.
- The Profile page calls `wx.requestSubscribeMessage` and records either accepted or rejected state, so users who reject subscription still keep the Today-page fallback.
- Today page now uses a gallery-style `swiper` with focused center card, single-line titles, 3-line summaries, progress strip, and non-blocking bottom action bar.
- Answer page includes a flip knowledge card, "易错考法" back side, soft correct/wrong states, and AI context handoff.
- Wrongbook page includes weak-chapter bars, filter mode tabs, error-reason tags, redo flow context, and AI wrong-question explanation prompts.

MySQL persistence (dual-impl, MyBatis-Plus):

- User-data repositories plus content catalog, AI review queue, and subscription preference have MyBatis-Plus implementations under the `local` profile; the in-memory implementations remain for the `standalone` profile / tests.
- Flyway `V2__persist_user_data.sql`, `V3__persist_content_catalog.sql`, and `V4__review_queue_and_subscription.sql` create the schema; the `local` profile connects via env-var-configured MySQL (`MYSQL_HOST` / `PORT` / `USER` / `PASSWORD` / `DATABASE`). HikariCP pool capped at 2 for remote / limited-connection instances.

## MySQL Persistence

The default `standalone` profile runs entirely in memory (no MySQL) — fast for development and `mvn test`. The `local` profile persists user-generated state and content catalog to MySQL via MyBatis-Plus (docs §2.1). Each repository has two implementations where needed: an in-memory one (`@Profile("standalone")`) and a MyBatis-Plus one (`@Profile("local")`).

Flyway manages the schema: `V1__init_schema.sql` (marker), `V2__persist_user_data.sql`, `V3__persist_content_catalog.sql`, and `V4__review_queue_and_subscription.sql`.

The database must exist before first start (Flyway does not create it):

```sql
CREATE DATABASE onboarding_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Run against your own MySQL (env vars override the docker-compose defaults; do not commit credentials):

```bash
MYSQL_HOST=... MYSQL_PORT=3306 MYSQL_USER=... MYSQL_PASSWORD=... MYSQL_DATABASE=onboarding_prod \
  mvn -f backend/onboarding-api/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

The HikariCP pool is capped at 2 (`spring.datasource.hikari.maximum-pool-size`) for remote / limited-connection MySQL instances; raise it for a local docker MySQL.
