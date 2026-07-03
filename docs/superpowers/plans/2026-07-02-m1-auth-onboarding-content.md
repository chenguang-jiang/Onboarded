# M1 Auth Onboarding Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first usable product entry path: login, onboarding state, study settings, and seed-content read APIs.

**Architecture:** Keep M1 as modular monolith slices. The backend exposes stable API contracts through controllers, with service interfaces that can run in `standalone` mode without MySQL and later swap to database implementations under the `local` profile. The Mini Program consumes these contracts and shows the first-run setup before the Today page.

**Tech Stack:** Spring Boot 3.5.x, Java 21 target, JUnit 5, MockMvc, native WeChat Mini Program, TypeScript, WXSS tokens.

---

## File Structure

```text
backend/onboarding-api/src/main/java/com/onboarding/
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── WxLoginRequest.java
│   ├── WxLoginResponse.java
│   ├── WechatSession.java
│   ├── WechatSessionClient.java
│   ├── MockWechatSessionClient.java
│   └── SimpleTokenService.java
├── user/
│   ├── UserAccount.java
│   ├── UserAccountRepository.java
│   ├── InMemoryUserAccountRepository.java
│   ├── UserOnboardingState.java
│   ├── UserOnboardingStateRepository.java
│   ├── InMemoryUserOnboardingStateRepository.java
│   ├── StudySettingsController.java
│   ├── StudySettingsRequest.java
│   └── StudySettingsResponse.java
└── syllabus/
    ├── ChapterController.java
    ├── ChapterResponse.java
    ├── KnowledgePointController.java
    ├── KnowledgePointResponse.java
    ├── QuestionController.java
    ├── QuestionResponse.java
    └── SeedSyllabusService.java

backend/onboarding-api/src/test/java/com/onboarding/
├── auth/AuthControllerTest.java
├── user/StudySettingsControllerTest.java
└── syllabus/SyllabusControllerTest.java

miniprogram/pages/onboarding/
├── index.json
├── index.ts
├── index.wxml
└── index.wxss
```

## Task 1: Login Contract

**Files:**
- Create backend auth and user repository files listed above.
- Test: `backend/onboarding-api/src/test/java/com/onboarding/auth/AuthControllerTest.java`

- [ ] Write failing MockMvc test for `POST /api/auth/wx-login` with code `dev-code-001`.
- [ ] Run `mvn test -Dtest=AuthControllerTest`; expected compile failure because controller does not exist.
- [ ] Implement minimal auth controller, service, mock WeChat client, in-memory user repository, onboarding state repository, and simple token service.
- [ ] Run `mvn test -Dtest=AuthControllerTest`; expected pass.

## Task 2: Study Settings And Onboarding Completion

**Files:**
- Create user settings files listed above.
- Test: `backend/onboarding-api/src/test/java/com/onboarding/user/StudySettingsControllerTest.java`

- [ ] Write failing test for `PUT /api/users/me/study-settings`.
- [ ] Implement settings update using current user token header.
- [ ] Mark onboarding complete after valid settings are saved.
- [ ] Run `mvn test -Dtest=StudySettingsControllerTest`; expected pass.

## Task 3: Seed Content Read APIs

**Files:**
- Create syllabus files listed above.
- Test: `backend/onboarding-api/src/test/java/com/onboarding/syllabus/SyllabusControllerTest.java`

- [ ] Write failing tests for chapter tree, knowledge point detail, and next question.
- [ ] Implement in-memory seed content for at least 3 chapters, 6 knowledge points, and 6 questions.
- [ ] Run `mvn test -Dtest=SyllabusControllerTest`; expected pass.

## Task 4: Mini Program First-Run Path

**Files:**
- Modify: `miniprogram/app.json`
- Create: `miniprogram/pages/onboarding/index.*`
- Modify: `miniprogram/pages/today/index.*`

- [ ] Add onboarding page before Today in `app.json`.
- [ ] Implement a lightweight onboarding setup screen using existing visual tokens.
- [ ] Add request helpers for login and study settings.
- [ ] Keep Today page usable with existing mock state until real daily task APIs arrive.

## Task 5: Verification

- [ ] Run `mvn test`.
- [ ] Run Mini Program JSON parse check.
- [ ] Run placeholder scan for docs/backend/miniprogram.
- [ ] Start backend and call `/api/health` and `/api/auth/wx-login`.

## Self-Review

Spec coverage:

- Covers M1 login, first-run onboarding, user learning settings, and seed content read APIs.
- Does not implement daily task generation, review queue execution, AI, wrongbook, or notifications; those belong to M2+.

Placeholder scan:

- No `TODO`, `TBD`, or deferred placeholders are used.

Type consistency:

- Login response includes `token`, `userId`, `openid`, and `onboardingRequired`.
- Study settings response includes `examDate`, `dailyTarget`, `reminderTime`, and `onboardingCompleted`.
