# AI Job Agent

A Java-based AI job search agent that automatically aggregates software engineering job postings from multiple sources, stores and deduplicates them in PostgreSQL, evaluates job fit using OpenAI, and sends high-match opportunities by email.

The application is containerized with Docker and deployed on AWS using ECS Fargate, RDS PostgreSQL, Secrets Manager, CloudWatch, and EventBridge Scheduler.

The current system runs automatically in the cloud on a daily schedule.

## Features

- Aggregates job postings from multiple sources:
  - Remotive
  - Remote OK

- Searches for software engineering roles using configurable keywords

- Stores job postings in PostgreSQL

- Prevents duplicate job insertion using external job IDs

- Uses a rule-based pre-filter to remove obviously irrelevant roles

- Filters out overly senior positions such as:
  - Senior
  - Staff
  - Principal
  - Lead
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

- Sends high-match opportunities through Gmail

- Uses PostgreSQL state tracking for persistent job history

- Packaged as a standalone executable JAR

- Containerized with Docker

- Stores Docker images in Amazon ECR

- Runs the application on Amazon ECS Fargate

- Uses Amazon RDS PostgreSQL as the cloud database

- Stores sensitive credentials in AWS Secrets Manager

- Sends application logs to Amazon CloudWatch

- Uses Amazon EventBridge Scheduler to automatically run the agent every day

- Uses IAM roles and policies for AWS service permissions

- Uses environment variables and Secrets Manager for configuration

- Safely closes the OpenAI client after execution

## Tech Stack

### Backend
- Java 21
- Maven
- PostgreSQL
- OpenAI Java SDK
- Jakarta Mail
- Java HTTP Client
- Jackson

### Cloud / DevOps
- Docker
- Amazon ECR
- Amazon ECS Fargate
- Amazon RDS PostgreSQL
- AWS Secrets Manager
- Amazon CloudWatch
- Amazon EventBridge Scheduler
- AWS IAM

### Development
- Git
- GitHub
- Windows PowerShell
- PostgreSQL / pgAdmin

## Architecture

```text
                Amazon EventBridge Scheduler
                           |
                           v
                    Amazon ECS Fargate
                           |
                           v
                    AI Job Agent
                           |
          +----------------+----------------+
          |                                 |
          v                                 v
      Remotive                          Remote OK
          \                                 /
           \                               /
            +-------> Job Ingestion <------+
                           |
                           v
                 Amazon RDS PostgreSQL
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
                  Gmail Notification
                           |
                           v
                  Sent Job Tracking

## AWS Architecture

GitHub / Local Development
          |
          v
       Docker
          |
          v
     Amazon ECR
          |
          v
   Amazon ECS Fargate
          |
          +----------------------+
          |                      |
          v                      v
   Amazon RDS             AWS Secrets Manager
   PostgreSQL             - OpenAI API Key
                          - DB Password
                          - Gmail App Password

          |
          v
     CloudWatch Logs

EventBridge Scheduler
          |
          v
   Triggers ECS Task
   Daily at 9:00 AM PT

##Job Processing Flow

1. Fetch jobs from Remotive and Remote OK
2. Save new jobs to PostgreSQL
3. Ignore duplicate external job IDs
4. Apply rule-based pre-filter
5. Load unscored jobs
6. Send relevant jobs to OpenAI
7. Generate match score, reason, and skill gap
8. Store AI results in PostgreSQL
9. Select jobs above the email threshold
10. Send high-match jobs by email
11. Record sent jobs to prevent duplicate notifications
12. Exit the ECS task

##Cloud Deployment

Source Code
   |
   v
Maven Build
   |
   v
Executable JAR
   |
   v
Docker Image
   |
   v
Amazon ECR
   |
   v
Amazon ECS Fargate
   |
   v
Amazon RDS PostgreSQL