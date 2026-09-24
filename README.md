# AI Job Agent

A Java-based AI job search agent that automatically aggregates software engineering job postings from multiple sources, stores and deduplicates them in PostgreSQL, evaluates job fit using OpenAI, and sends high-match opportunities by email.

The application is also containerized with Docker so the same workflow can run consistently across different environments.

## Features

- Aggregates job postings from multiple sources:
  - Remotive
  - Remote OK
- Searches for software engineering roles using configurable keywords
- Stores jobs in PostgreSQL
- Prevents duplicate job insertion using external job IDs
- Uses a rule-based pre-filter to remove obviously irrelevant roles
- Filters out overly senior positions such as:
  - Senior
  - Staff
  - Principal
  - Manager
  - Director
  - Architect
- Uses OpenAI to generate:
  - Match score
  - Match reason
  - Skill gap
- Automatically retries jobs that were not successfully scored
- Marks pre-filtered jobs so they are not repeatedly processed
- Filters jobs by AI match score before email delivery
- Tracks previously sent jobs to prevent duplicate notifications
- Supports scheduled execution using Windows Task Scheduler
- Uses environment variables for credentials and secrets
- Writes execution logs for scheduled runs
- Safely closes the OpenAI client after execution
- Packaged as a standalone executable JAR
- Containerized with Docker

## Tech Stack

- Java 21
- Maven
- PostgreSQL
- OpenAI Java SDK
- Jakarta Mail
- Java HTTP Client
- Jackson
- Docker
- Windows Task Scheduler
- Git / GitHub

## Architecture

```text
Remotive --------\
                  \
                   -> Job Ingestion
                  /
Remote OK -------/
        |
        v
PostgreSQL
        |
        v
Deduplication
        |
        v
Rule-Based Pre-Filter
        |
        v
Find Unscored Jobs
        |
        v
OpenAI Job Matching
        |
        v
Store Score / Reason / Gap
        |
        v
Match Score Filter
        |
        v
Email Notification
        |
        v
Sent Job Tracking