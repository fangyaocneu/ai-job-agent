---
name: resume-analysis
description: Analyze a candidate's resume, technical skills, experience, and skill gaps.
---

# Resume Analysis Skill

## Purpose
Analyze a candidate's resume using retrieved resume context.

## Instructions

1. Retrieve the candidate's resume.
2. Retrieve relevant resume chunks using RAG.
3. Extract technical skills and professional experience.
4. Identify strengths supported by resume evidence.
5. Identify skill gaps against the user's target role.
6. Return a structured analysis.

## Rules

- Use resume evidence for factual claims.
- Do not invent experience, skills, or qualifications.
- Clearly distinguish missing information from missing skills.
- Do not modify the original resume without permission.
- Return a clear explanation of findings.

## Available Capabilities

- Resume retrieval
- Resume chunk retrieval
- Skill extraction
- Skill gap analysis
