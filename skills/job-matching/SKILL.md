---
name: job-matching
description: Evaluate candidate suitability for a job using resume evidence and job requirements.
---

# Job Matching Skill

## Purpose
Evaluate the compatibility between a candidate and a job description.

## Instructions

1. Retrieve the candidate's resume and profile.
2. Retrieve the target job description.
3. Extract job requirements.
4. Compare candidate skills with required skills.
5. Evaluate relevant experience and role fit.
6. Calculate a weighted matching score.
7. Explain strengths, gaps, and recommendations.

## Scoring

Use the existing Java scoring system:

- Skill Match: 40%
- Experience Match: 25%
- Role Fit: 20%
- Preference Match: 15%

The final weighted score must be calculated by Java,
not invented directly by the LLM.

## Rules

- Never fabricate candidate qualifications.
- Keep scoring deterministic.
- Support conclusions with candidate and job evidence.
- Explain missing qualifications clearly.
- Do not automatically apply for a job.
