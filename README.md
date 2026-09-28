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


-----------
##Multi-Agent AI Workflow

PreFilterAgent
      ↓
JobAnalysisAgent
      ↓
MatchAgent
      ↓
StrategyAgent

1. PreFilterAgent
The PreFilter Agent determines whether a job should continue through the AI pipeline.
Responsibilities include:
- Role relevance filtering
- Location validation
- Seniority filtering
- Early rejection of obviously irrelevant jobs
Jobs rejected here avoid unnecessary OpenAI API calls.

2. JobAnalysisAgent
The Job Analysis Agent converts an unstructured job description into structured information.
It extracts:
- Role type
- Seniority
- Primary skills
- Secondary skills
- Required years of experience
- Job summary

3. MatchAgent
The Match Agent compares the structured job requirements with the candidate profile.
It produces:
- Overall match score
- Skill score
- Experience score
- Role-fit score
- Match reason
- Strengths
- Missing skills

4. StrategyAgent
The Strategy Agent decides how the candidate should approach the opportunity.
It produces:
- Recommendation
- Application priority
- Resume focus
- Concerns
- Application advice


##Agent Orchestration
                AgentCoordinator
                       │
                       ▼
                 AgentContext
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Job Data       AI Results      Agent State

##System Architecture

                        JOB SOURCES
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
      Remotive           RemoteOK          Greenhouse
                                                │
                                                ▼
                                              Lever
                             │
                             ▼
                    JobSource Interface
                             │
                             ▼
                     Job Ingestion Layer
                             │
                             ▼
                        PostgreSQL
                             │
                             ▼
                      AgentCoordinator
                             │
                             ▼
                      PreFilterAgent
                             │
                             ▼
                     JobAnalysisAgent
                             │
                             ▼
                        MatchAgent
                             │
                             ▼
                      StrategyAgent
                             │
             ┌───────────────┼───────────────┐
             │               │               │
             ▼               ▼               ▼
     job_agent_results   agent_runs         Email
             │               │
             └───────┬───────┘
                     ▼
              Spring Boot API
                     │
                     ▼
               React Dashboard

##Daily Automation Workflow

DailyJobRunner
      ↓
Search Job Sources
      ↓
Normalize Jobs
      ↓
Deduplicate Jobs
      ↓
Save to PostgreSQL
      ↓
Select Unscored Jobs
      ↓
PreFilter
      ↓
AgentCoordinator
      ↓
JobAnalysisAgent
      ↓
MatchAgent
      ↓
StrategyAgent
      ↓
Persist Results
      ↓
Send Email
      ↓
Update Dashboard