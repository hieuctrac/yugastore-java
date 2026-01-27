---
name: Business Requirements Document
description: Generate a current-state business requirements document from codebase analysis
---

Analyze the codebase and create a Business Requirements Document that:

## 1. Documents ONLY Current State
- Document what is implemented right now
- No future requirements, no gap analysis, no recommendations
- No priorities (P0/P1/P2) or roadmaps
- No target state or "should have" language

## 2. Uses Business Language Exclusively
- Avoid all technology-specific terms (PostgreSQL, Cassandra, REST, microservices, etc.)
- Use generic terms: "data store" not "PostgreSQL", "service" not "REST endpoint"
- No code references: no file paths, no method names, no line numbers
- No implementation details: no SQL queries, no class names, no technical architecture

## 3. Organized by Business Domains
Structure the document with these sections:
- Shopping Cart Rules
- Product Catalog Rules  
- Checkout and Order Rules
- Inventory Management Rules
- Pricing Rules
- User Management Rules
- Transaction Integrity
- Not Implemented (capabilities that don't exist)

## 4. Format Each Rule As
- "The system does X when Y"
- Focus on business behavior and outcomes
- Describe what happens from a user/business perspective

## 5. Include
- Document header (version, owner, purpose)
- Overview section explaining scope
- One section per business domain with subsections
- "Not Implemented" section listing missing capabilities
- Document control (change log, related documents)

## 6. Exclude
- All code file references and paths
- Technology stack details
- Database schema specifics
- API endpoint patterns
- Implementation methods or functions
- Performance metrics or technical specifications

## Output Requirements
The output should read like a business requirements document that any stakeholder could understand without technical knowledge, while accurately describing the current business rules and capabilities.

Save the document to: `docs/business-requirements.md`
