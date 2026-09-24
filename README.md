# AI Job Agent

A Java-based AI job search agent that automatically retrieves software engineering job postings, stores and deduplicates them in PostgreSQL, evaluates job fit using OpenAI, and sends high-match opportunities by email.

## Features

- Searches for software engineering jobs using configurable keywords
- Stores jobs in PostgreSQL
- Prevents duplicate job insertion using external job IDs
- Uses a rule-based pre-filter to skip obviously irrelevant jobs
- Uses OpenAI to generate:
  - Match score
  - Match reason
  - Skill gap
- Automatically retries previously unscored jobs
- Filters jobs by AI match score before sending email
- Tracks previously sent jobs to prevent duplicate notifications
- Supports scheduled execution using Windows Task Scheduler
- Uses environment variables for credentials and secrets
- Writes execution logs for scheduled runs
- Safely closes the OpenAI client after execution

## Tech Stack

- Java 21
- Maven
- PostgreSQL
- OpenAI Java SDK
- Jakarta Mail
- Java HTTP Client
- Windows Task Scheduler

## Workflow

```text
Job Search API
      |
      v
Search Jobs
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