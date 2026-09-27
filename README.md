# AI Job Agent

An AI-powered job search and tracking platform built with Java, Spring Boot, React, PostgreSQL, OpenAI, Docker, and AWS.

The system automatically collects job postings, removes duplicates, applies rule-based filtering, uses AI to score candidate-job fit, sends high-match jobs by email, and provides a web dashboard for managing job applications.

---

## Features

### Automated Job Search
- Collects jobs from multiple sources
- Currently supports:
  - Remotive
  - RemoteOK
- Deduplicates jobs using external job IDs
- Stores job data in PostgreSQL

### AI Job Matching
- Rule-based pre-filtering removes obviously irrelevant roles
- OpenAI evaluates candidate-job fit
- Generates:
  - Match score
  - Match reason
  - Skill / experience gap
- High-match jobs can automatically trigger email notifications

### Job Tracking Dashboard
React dashboard provides:

- Total jobs
- High-match jobs
- Unscored jobs
- Average match score
- Search by title, company, or location
- Filter by minimum score
- Sort by match score
- Favorite jobs
- Mark jobs as applied
- Ignore jobs

Job tracking state is persisted in PostgreSQL.

---

## Architecture

```text
                        ┌─────────────────────┐
                        │     Job Sources     │
                        │ Remotive / RemoteOK │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │ Java Job Ingestion  │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │ PostgreSQL / RDS    │
                        └──────────┬──────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
                    ▼                             ▼
          ┌─────────────────────┐       ┌─────────────────────┐
          │ Rule-based Filter   │       │ Spring Boot REST API│
          └──────────┬──────────┘       └──────────┬──────────┘
                     │                             │
                     ▼                             ▼
          ┌─────────────────────┐       ┌─────────────────────┐
          │ OpenAI Job Scoring  │       │ React Dashboard     │
          └──────────┬──────────┘       └─────────────────────┘
                     │
                     ▼
          ┌─────────────────────┐
          │ Email Notifications │
          └─────────────────────┘


## AWS Production Architecture

                          Internet
                             │
                             ▼
                       CloudFront
                      /          \
                     /            \
                    ▼              ▼
              S3 React App       /api/*
                                   │
                                   ▼
                      Application Load Balancer
                                   │
                                   ▼
                         ECS Fargate Service
                                   │
                                   ▼
                          Spring Boot API
                                   │
                                   ▼
                              AWS RDS

##AWS Services Used
- Amazon ECR
  - Stores Docker images
- Amazon ECS Fargate
  - Runs scheduled job-processing tasks
  - Runs the Spring Boot REST API service
- Amazon RDS PostgreSQL
  - Stores job listings and tracking state
- AWS Secrets Manager
  - Stores sensitive credentials such as:
    - OpenAI API key
    - Database password
    - Email app password
- Amazon EventBridge Scheduler
  - Runs the job search agent automatically every day
- Amazon CloudWatch
  - Stores ECS application logs
- Application Load Balancer
  - Routes public API traffic to the Spring Boot ECS service
- Amazon S3
  - Hosts the production React frontend
- Amazon CloudFront
  - Serves the React application
  - Routes /api/* traffic to the backend load balancer
  - Provides HTTPS access to the application

  # AI Job Agent

An AI-powered job search, matching, and application-management platform built with Java, Spring Boot, React, PostgreSQL, OpenAI, Docker, and AWS.

The system automatically collects job postings, normalizes and deduplicates them, applies rule-based filtering, runs a multi-agent AI workflow to analyze candidate-job fit, sends email notifications, and provides a web dashboard for tracking applications and follow-ups.


----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
## Current Status

- Phase 1 — Job ingestion ✅
- Phase 2 — AI scoring + scheduled automation ✅
- Phase 3 — Full-stack dashboard + AWS deployment ✅
- Phase 4 — Application tracking + follow-up reminders ✅
- Phase 5 — Multi-agent workflow 🚧 In Progress

Phase 5 currently includes a working local agent chain:

```text
PreFilterAgent
      ↓
JobAnalysisAgent
      ↓
MatchAgent
      ↓
StrategyAgent

## Phase 5 Architecture

                 JOB SOURCE LAYER
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
  RemotiveJobSource           RemoteOkJobSource
          │                           │
          └─────────────┬─────────────┘
                        ▼
                JobSource Interface
                        │
                        ▼
              JobIngestionService
                        │
                        ▼
                 PostgreSQL / RDS
                        │
                        ▼
                   AgentContext
                        │
                        ▼
                PreFilterAgent
                        │
                        ▼
               JobAnalysisAgent
                        │
               structured analysis
                        │
                        ▼
                   MatchAgent
                        │
              MatchEvaluation
                        │
                        ▼
                 StrategyAgent
                        │
                        ▼
             ApplicationStrategy
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
         DB         Dashboard       Email

## Application Workflow

DailyJobRunner
      ↓
DailyJobWorkflow
      ↓
JobIngestionService
      ↓
PreFilterAgent
      ↓
JobAnalysisAgent
      ↓
MatchAgent
      ↓
StrategyAgent
      ↓
Database + Email + Dashboard