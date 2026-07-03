# Code Review And SQL Summary

Date: 2026-07-02

## Current Execution Point

The project has moved beyond the original M1 foundation. The local codebase now includes:

- WeChat login abstraction: local mock by default, real `jscode2session` switch via environment variables.
- First-run onboarding: exam date, daily target, reminder time.
- Daily plan loop: `GET /api/today`, item start, item complete.
- Practice answering: question fetch and answer submission.
- Wrongbook loop: wrong answer auto-archive, chapter grouping, redo, manual mastered, two-correct auto-mastered.
- Mastery progress: overview and weak chapter summaries.
- AI Q&A: session create/list, messages, ask, mock GLM by default and real GLM switch.
- MySQL persistence layer for user-generated state and content catalog under the `local` profile.

## Review Findings Fixed

### 1. Forged dev token could create data for a non-existing user

Before the fix, any request with `Authorization: Bearer dev-token-9999` could pass token parsing and generate a daily plan for user `9999`.

Fix:

- `SimpleTokenService.requireUserId` now parses the token and verifies that the user exists through `UserAccountRepository`.
- Added regression coverage in `TodayControllerTest#getTodayRejectsTokenForMissingUser`.

### 2. MySQL mapping mismatch for `daily_plan_item.source_type`

The table column is `source_type`, but `DailyPlanItemEntity` used the Java field `source` without an explicit mapping. In MyBatis local mode this would attempt to read/write a non-existing `source` column.

Fix:

- Added `@TableField("source_type")` to `DailyPlanItemEntity.source`.

## Closed Loops

### Login And Onboarding

Closed:

- Mini Program calls `wx.login`.
- Backend exchanges code through mock/real WeChat session client.
- User account and onboarding state are created.
- Study settings complete onboarding.
- Frontend stores token and settings.

Remaining:

- Session tokens currently have fixed 30-day TTL; logout and forced invalidation endpoints are not implemented yet.

### Daily Study

Closed:

- First `GET /api/today` generates a 15-item daily plan.
- Repeated calls for the same user and day reuse the same plan.
- Items can move from `PENDING` to `STUDYING` to `DONE`.
- Correct answer from the answer page completes the item.
- Weak and wrong-related knowledge points are prioritized.

Remaining:

- Carry-over is implemented from historical unfinished daily-plan items. Push notification authorization is persisted, while real WeChat template sending still needs the production template ID.

### Practice And Wrongbook

Closed:

- Question detail does not expose answer key before submission.
- Wrong answers create or update a wrongbook item.
- Correct answers do not create wrongbook items.
- Two consecutive correct redos auto-mark mastered.
- Mastered items are hidden from default wrongbook list.

Remaining:

- Wrongbook currently has no explicit search or mastered history page in the Mini Program.

### AI Q&A

Closed:

- Session creation/listing, message listing, and ask endpoint exist.
- Mock GLM keeps local development runnable.
- Real GLM client is switchable via env vars.
- Foreign session access returns 404.
- Assistant messages can be added to the review queue through `POST /api/ai/messages/{id}/review-items`.
- Mini Program AI page supports context handoff from Today, Answer, and Wrongbook plus "加入复习".

Remaining:

- Real GLM retrieval needs live validation after `GLM_API_KEY` and `GLM_KNOWLEDGE_ID` are configured.
- References are currently parsed as a simple list; richer citation metadata can be added later.

### Subscription Reminder

Closed:

- Profile page calls `wx.requestSubscribeMessage`.
- Accepted and rejected decisions are saved through `PUT /api/notifications/subscription`.
- Rejected subscription still keeps the Today page as the fallback reminder surface.
- MySQL table `subscription_preference` is created by Flyway V4.

Remaining:

- Replace the development template placeholder with an approved WeChat subscription template ID before live sending.
- Real WeChat access-token caching and message delivery client are not wired yet.

### UI Interaction

Closed:

- Today page uses gallery `swiper` cards with focused scale and non-wrapping titles.
- Answer page includes knowledge-card flip, easy-mistake side, soft answer states, and expanding analysis.
- Wrongbook page includes weak-chapter bars, mode tabs, error-reason tags, and AI explanation prompts.
- AI page uses knowledge-base-style answer layout, quick prompt chips, reference cards, and review action.

### MySQL Persistence

Closed:

- Flyway migrations define user, onboarding, answer, wrongbook, mastery, daily plan, and AI chat tables.
- Flyway migrations define chapter, knowledge point, practice question, and option tables with seed data.
- Flyway migrations define AI review queue and subscription preference tables.
- MyBatis repositories are selected under `local`; in-memory repositories are selected under `standalone`.
- Consolidated SQL is saved at `docs/sql/onboarding_mysql_schema.sql`.

Remaining:

- Local MySQL startup was not verified in this review because the current environment does not expose Docker validation here.

## SQL Files

- Flyway migration 1: `backend/onboarding-api/src/main/resources/db/migration/V1__init_schema.sql`
- Flyway migration 2: `backend/onboarding-api/src/main/resources/db/migration/V2__persist_user_data.sql`
- Flyway migration 3: `backend/onboarding-api/src/main/resources/db/migration/V3__persist_content_catalog.sql`
- Flyway migration 4: `backend/onboarding-api/src/main/resources/db/migration/V4__review_queue_and_subscription.sql`
- Consolidated MySQL script: `docs/sql/onboarding_mysql_schema.sql`

## Verification

Executed:

```bash
cd backend/onboarding-api
mvn test
```

Result:

- 42 tests passed.
- 0 failures.
- 0 errors.

Also checked:

- Mini Program page registration includes onboarding, today, answer, AI chat, wrongbook, profile.
- Source code no longer calls the removed `/api/study/today` endpoint.
