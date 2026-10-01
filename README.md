# AI Job Agent

An AI-powered career agent platform for job discovery, resume intelligence, candidate-job matching, application tracking, and conversational career assistance, built with Java, Spring Boot, React, PostgreSQL, OpenAI, Docker, and AWS.

The system ingests and deduplicates job postings from multiple sources, analyzes candidate-job fit through a multi-stage AI workflow, supports resume upload and RAG-based semantic retrieval, routes user requests through intent-aware tool selection, tracks applications and follow-ups, and persists conversations and agent execution traces for full-stack observability. The production application is deployed on AWS using CloudFront, ECS Fargate, RDS PostgreSQL, and related cloud services.

---

## Current Status

- Phase 1 — Job ingestion — Done
- Phase 2 — AI scoring + scheduled automation — Done
- Phase 3 — Full-stack dashboard + AWS deployment — Done
- Phase 4 — Application tracking + follow-up reminders — Done
- Phase 5 — Multi-agent workflow + observability — Done
- Phase 6 — Production AI chat + Resume RAG + intent routing — Done
- Phase 7 — Multi-user SaaS architecture — Planned

Phase 5 includes:

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
```

---

## Multi-Agent AI Workflow

```text
PreFilterAgent
      ↓
JobAnalysisAgent
      ↓
MatchAgent
      ↓
StrategyAgent
```

### 1. PreFilterAgent

The PreFilter Agent determines whether a job should continue through the AI pipeline.

Responsibilities include:

- Role relevance filtering
- Location validation
- Seniority filtering
- Early rejection of obviously irrelevant jobs

Jobs rejected here avoid unnecessary OpenAI API calls.

### 2. JobAnalysisAgent

The Job Analysis Agent converts an unstructured job description into structured information.

It extracts:

- Role type
- Seniority
- Primary skills
- Secondary skills
- Required years of experience
- Job summary

### 3. MatchAgent

The Match Agent compares the structured job requirements with the candidate profile.

It produces:

- Overall match score
- Skill score
- Experience score
- Role-fit score
- Match reason
- Strengths
- Missing skills

### 4. StrategyAgent

The Strategy Agent decides how the candidate should approach the opportunity.

It produces:

- Recommendation
- Application priority
- Resume focus
- Concerns
- Application advice

---

## Agent Orchestration

```text
                AgentCoordinator
                       │
                       ▼
                 AgentContext
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   Job Data       AI Results      Agent State
```

---

## System Architecture

```text
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
```

---

## Daily Automation Workflow

```text
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
```

---

# Phase 6 — Production AI Chat & AWS Deployment

Phase 6 brings the AI Job Agent from a local application to a production-ready conversational AI system deployed on AWS.

## Production Architecture

```text
User / React Frontend
        ↓
Amazon CloudFront
        ↓
Application Load Balancer
        ↓
Amazon ECS Fargate
        ↓
Spring Boot API
        ↓
┌───────────────────────┬──────────────────────┐
│ OpenAI API            │ PostgreSQL / RDS     │
│ Reasoning + Tool Use  │ Conversations        │
│                       │ Profiles / Resume RAG│
└───────────────────────┴──────────────────────┘
```

## Phase 6 Features

- Deployed the Spring Boot backend to **Amazon ECS Fargate**
- Deployed the frontend behind **Amazon CloudFront**
- Added **Application Load Balancer** routing and `/api/health` health checks
- Connected production services to **Amazon RDS PostgreSQL**
- Added **AWS Secrets Manager** for database and OpenAI credentials
- Added persistent **chat conversations and messages**
- Added conversational `/api/chat` and `/api/conversations` APIs
- Added OpenAI **tool calling**
- Added resume storage, chunking, embeddings, and **RAG-based resume retrieval**
- Added conversation title generation
- Added persistent agent execution traces and tool-use metadata
- Added Agent Activity display in the React chat interface
- Verified end-to-end AI chat in the AWS production environment
- Configured CloudFront origin timeout for longer AI reasoning requests

---

## Resume RAG

Users can upload a PDF resume through the React frontend.

The resume is processed through the following pipeline:

```text
Resume PDF
     ↓
Text Extraction
     ↓
Chunking
     ↓
Embedding Generation
     ↓
PostgreSQL
     ↓
Semantic Search
```

When the AI agent needs resume information:

```text
User Question
     ↓
SearchResume
     ↓
Query Embedding
     ↓
Vector Similarity Search
     ↓
Relevant Resume Chunks
     ↓
OpenAI Final Answer
```

The retrieved evidence includes:

- Resume ID
- Chunk index
- Similarity score
- Retrieved resume content

This allows answers to be grounded in actual resume evidence instead of unsupported assumptions.

---

## Jev Intent Routing

Phase 6 was extended with a lightweight intent-routing layer before the main OpenAI reasoning loop.

The router classifies each user message into one of the following intents:

- `RESUME`
- `JOB_SEARCH`
- `JOB_MATCH`
- `APPLICATION`
- `GENERAL`

The routing flow is:

```text
User Message
    ↓
Jev Intent Router
    ↓
Intent Classification
    ↓
Tool Narrowing
    ↓
OpenAI Reasoning
    ↓
Tool Execution
    ↓
Final Response
```

Example:

```text
"Based on my resume, what backend experience do I have?"

        ↓

Jev Router

Intent: RESUME

        ↓

Available Tools

SearchResume
GetCandidateProfile

        ↓

OpenAI Reasoning

        ↓

SearchResume

        ↓

Resume RAG Evidence

        ↓

Final Answer
```

This reduces the number of irrelevant tools exposed to the main LLM while preserving OpenAI's ability to decide whether and how to call the available tools.

---

## Intent-Based Tool Narrowing

The available tool set is dynamically selected based on the detected intent.

```text
RESUME
    ↓
SearchResume
GetCandidateProfile

JOB_SEARCH
    ↓
GetTopMatches

JOB_MATCH
    ↓
GetJobInsights
SearchResume
AnalyzeResumeGap

APPLICATION
    ↓
GetApplications
GetFollowUps

GENERAL
    ↓
No application-specific tools
```

If intent routing fails, the system can fall back to the broader original tool set.

---

## Persistent Agent Activity

Agent tool executions are persisted in PostgreSQL using the `agent_tool_calls` table.

Each trace stores:

- Tool name
- Tool arguments
- Tool result
- Associated assistant message
- Execution timestamp

The persistence flow is:

```text
Assistant Response
      ↓
AgentTrace
      ↓
agent_tool_calls
      ↓
Conversation Reload
      ↓
Agent Activity Restored
```

This allows tool execution history to remain visible after page refreshes and conversation reloads.

---

## Agent Activity UI

The React chat interface exposes agent execution details directly below assistant responses.

Example:

```text
Agent Activity

🧭 Jev Router
Intent
RESUME

⚙ SearchResume
Query
backend experience Spring Boot REST APIs...

Requested Results
3

Retrieved Evidence
Resume 1 / Chunk 1 / Similarity 0.416
...
```

The Jev Router trace shows the selected intent, while `SearchResume` exposes the query, requested result count, and retrieved resume evidence.

---

## End-to-End Conversational Agent Flow

```text
User
  ↓
React Chat UI
  ↓
Spring Boot ChatService
  ↓
Jev Intent Router
  ↓
Intent-Based Tool Narrowing
  ↓
OpenAI Reasoning
  ↓
Tool Call
  ↓
Resume RAG / Application Data / Job Data
  ↓
OpenAI Final Response
  ↓
chat_messages
  ↓
agent_tool_calls
  ↓
Persistent Agent Activity UI
```

---

## Database

Major tables currently include:

```text
jobs
sent_jobs
candidate_profile
job_agent_results
agent_runs
resumes
resume_chunks
chat_conversations
chat_messages
agent_tool_calls
```

---

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Maven
- JDBC
- PostgreSQL
- OpenAI Java SDK

### AI

- OpenAI API
- Tool calling
- LLM-based job analysis
- Jev intent routing
- Embeddings
- Retrieval-Augmented Generation
- Deterministic scoring

### Frontend

- React
- Vite
- JavaScript
- React Markdown

### Infrastructure

- Docker
- AWS ECS Fargate
- Amazon ECR
- Amazon RDS
- Amazon S3
- Amazon CloudFront
- Application Load Balancer
- Amazon CloudWatch
- AWS Secrets Manager

---

## Remaining Phase 6 Improvements

Planned improvements include:

- Jev confidence visibility
- Graceful fallback when Jev is unavailable
- Lower agent latency
- Streaming responses
- Improved resume retrieval ranking
- Hybrid semantic and keyword retrieval
- Authorization-aware RAG

---

# Phase 7 — Multi-User SaaS Architecture

Planned Phase 7 work includes:

- Multi-user authentication
- User-level data isolation
- Role-based authorization
- Authorization-aware RAG
- Background task queues
- Horizontal scaling
- Rate limiting
- Caching
- Multi-tenant SaaS architecture