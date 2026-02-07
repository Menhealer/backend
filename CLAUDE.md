# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Relog is a Spring Boot 3.4.3 REST API for relationship management — tracking friends, events, gifts, and generating analytics/insights. Java 17, Gradle 8.10, PostgreSQL, Redis, AWS S3.

## Build & Run Commands

```bash
# Build (skip tests — no tests exist yet)
./gradlew build -x test

# Run locally (requires PostgreSQL on localhost:5432/relog, Redis on localhost:6379)
./gradlew bootRun

# Run with production profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# Build JAR
./gradlew bootJar
# Output: build/libs/relog-0.0.1-SNAPSHOT.jar

# Docker
docker build -t relog-backend .
```

## Architecture

**Domain-driven layered architecture** under `com.relog.relog`:

| Package | Purpose |
|---------|---------|
| `auth` | JWT-based signup/login, email validation, password change |
| `member` | User profiles, S3 image upload |
| `friend` | Friend CRUD, linked to member |
| `friendgroup` | Optional friend categorization |
| `event` | Meetings with friends, ReviewScore (1-5 rating), calendar queries |
| `gift` | Gift tracking with GiftType/GiftDirection enums |
| `settlement` | Monthly/quarterly analytics — top friends, maintenance suggestions, AI insights |
| `ai` | Rule-based relationship analysis (interface-based, designed for future ML swap) |
| `jwt` | JwtUtil, JwtAuthenticationFilter, refresh tokens in Redis |
| `storage` | S3StorageService for file uploads |
| `config` | SecurityConfig, RedisConfig, QueryDslConfig, S3Config |
| `common` | ApiResponse wrapper, BaseEntity (soft delete + audit), GlobalExceptionHandler |

**Domain model:** RelogMember → Friend → Event (with ReviewScore) / Gift (with type & direction). Friends optionally belong to FriendGroup.

## Key Patterns

- **Soft delete:** All entities extend `BaseEntity` with `is_deleted` flag. Uses `@SQLRestriction("is_deleted = false")` and `@SQLDelete` annotations — never hard-delete.
- **API responses:** All controllers return `ApiResponse<T>` (standardized wrapper).
- **Auth:** `@AuthenticationPrincipal Long memberId` in controller methods extracts user from JWT.
- **QueryDSL:** Complex queries use `XxxRepositoryCustom` interface + `XxxRepositoryImpl`. Q-classes generated in `src/main/generated/` (gitignored).
- **Refresh tokens:** Stored in Redis with memberId as key.
- **Auditing:** `@EnableJpaAuditing` with `created_at`/`updated_at` via `BaseEntity`.

## Configuration

- `application.yml` — local dev config (gitignored, has hardcoded credentials)
- `application-prod.yml` — production config using environment variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION`, `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`
- Production uses `ddl-auto: validate` (no auto-migration tool — schema must match entities)

## Development Rules

- **예외 케이스 및 검증:** 기능 및 요구사항을 구현할 때, 해당 기능에 대한 예외 케이스와 입력값 검증을 반드시 함께 구현한다. (잘못된 입력, 권한 없음, 존재하지 않는 리소스, 중복 등)
- **보안:** 코드 작성 시 SQL Injection, XSS 등 OWASP 주요 보안 취약점에 항상 신경 써서 작성한다. 사용자 입력은 반드시 검증하고, 쿼리 파라미터는 파라미터 바인딩을 사용한다.

## CI/CD

GitHub Actions (`.github/workflows/gradle.yml`): push/PR to `main`에서 트리거. Build → Docker build (linux/arm64) → Docker Hub push. Deploy는 push to main일 때만 실행 — SSH로 서버 접속 후 docker-compose pull/up.
- Deploy target: Oracle Cloud Infrastructure (ARM64 Ubuntu), docker-compose based.