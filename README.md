# AI Job Agent

An AI-powered job search, matching, application-management, and multi-agent orchestration platform built with Java, Spring Boot, React, PostgreSQL, OpenAI, Docker, and AWS.

The system automatically collects job postings from multiple sources, normalizes and deduplicates them, applies rule-based filtering, runs a multi-agent AI workflow to analyze candidate-job fit, sends high-match opportunities by email, tracks application progress and follow-ups, and exposes agent execution history through a full-stack dashboard.

---

## Current Status

- Phase 1 — Job ingestion ✅
- Phase 2 — AI scoring + scheduled automation ✅
- Phase 3 — Full-stack dashboard + AWS deployment ✅
- Phase 4 — Application tracking + follow-up reminders ✅
- Phase 5 — Multi-agent workflow + observability 🚧 Nearly Complete
- Phase 6 — Personalization ⏳
- Phase 7 — Multi-user SaaS architecture ⏳

Phase 5 currently includes:

- Multi-source job ingestion
- Multi-agent orchestration
- Structured AI job analysis
- Candidate-job match scoring
- Application strategy generation
- Retry and failure handling
- Persistent agent results
- Agent execution observability
- Agent Insights frontend
- Agent monitoring dashboard

---

# Features

## Automated Job Search

The system automatically collects job postings from multiple sources.

Currently supported sources:

- Remotive
- RemoteOK
- Greenhouse
- Lever

All sources implement a common `JobSource` abstraction.

```text
JobSource
   │
   ├── RemotiveJobSource
   ├── RemoteOkJobSource
   ├── GreenhouseJobSource
   └── LeverJobSource